#!/usr/bin/env bash
set -euo pipefail

# Usage (files):
#   ./marc2gcs.sh --authority-id GB-UkLiD --license CC0-1.0 rec1.json rec2.json
#
# Usage (stdin):
#   cat rec.json | ./marc2gcs.sh --authority-id GB-UkLiD --license CC0-1.0
#   zcat records.ndjson.gz | ./marc2gcs.sh --authority-id GB-UkLiD --license CC0-1.0 -
#
# Notes:
# - If no files are provided, the script reads from STDIN.
# - If a single "-" is provided, the script reads from STDIN.
# - jq happily processes multiple concatenated JSON docs or NDJSON.

AUTHORITY_ID=""
AUTHORITY_SCHEME="INTERNAL"
DOMAIN="bibliographic"
LICENSE="CC0-1.0"
LICENSE_URI=""
SIGNATORY=""
SRC_URI_PREFIX=""
NOW_ISO="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --authority-id) AUTHORITY_ID="$2"; shift 2;;
    --authority-scheme) AUTHORITY_SCHEME="$2"; shift 2;;
    --domain) DOMAIN="$2"; shift 2;;
    --license) LICENSE="$2"; shift 2;;
    --license-uri) LICENSE_URI="$2"; shift 2;;
    --signatory) SIGNATORY="$2"; shift 2;;
    --source-uri-prefix) SRC_URI_PREFIX="$2"; shift 2;;
    --) shift; break;;
    -*) echo "Unknown option: $1" >&2; exit 2;;
    *) break;;
  esac
done

if [[ -z "${AUTHORITY_ID}" || -z "${LICENSE}" ]]; then
  echo "ERROR: --authority-id and --license are required" >&2
  exit 2
fi

read -r -d '' JQ_PROG <<'JQ'
def fields: .record.fields;
def leader: .record.leader;

def ctrl($tag): [ fields[] | select(has($tag)) | .[$tag] | select(type=="string") ];
def dfo($tag):  [ fields[] | select(has($tag)) | .[$tag] | select(type=="object") ];
def sf_vals($tag; $code): [ dfo($tag)[]?.subfields[]? | select(has($code)) | .[$code] ];
def sf1($tag; $code): (sf_vals($tag;$code) | first) // null;

def dfo_first_ind2($tag; $ind2):
  ( if $ind2 == null then (dfo($tag) | first)
    else (dfo($tag) | map(select(.ind2==$ind2)) | first)
    end ) // null ;

def publication_block:
  ( dfo_first_ind2("264"; "1") // (dfo_first_ind2("260"; null)) ) as $pub |
  {
    place:     ( [ $pub.subfields[]? | (.a? // empty) ] | map(tostring) | unique ) // [],
    publisher: ( [ $pub.subfields[]? | (.b? // empty) ] | map(tostring) | unique ) // [],
    date:      ( [ $pub.subfields[]? | (.c? // empty) ] | first ) // null
  };

def year_guess:
  ( publication_block.date | tostring | capture("(?<y>\\d{4})")?.y ) // (
    ( ctrl("008") | first ) as $f008
    | if $f008 then ($f008 | tostring | .[7:11]) else null end
  ) | tonumber? ;

def languages_block:
  ( sf_vals("041"; "a") | map(tostring) | unique ) as $langs
  | if ($langs|length) > 0 then $langs
    else ( (ctrl("008")|first) as $f008
           | if $f008 and ($f008|length)>=38 then [ ($f008|.[35:38]) ] else [] end )
    end ;

def title_main: (sf1("245";"a") // null) as $a | (sf1("245";"b") // null) as $b
  | if $a and $b then ($a + " " + $b) else $a // $b end ;
def title_uniform:  (sf1("240";"a") // sf1("130";"a"));
def titles_array:
  ( [ {value: (title_main),     type:"main"}     | select(.value!=null) ]
  + [ {value: (title_uniform),  type:"uniform"}  | select(.value!=null) ]
  + [ sf_vals("246";"a")[]? | {value:., type:"alternative"} ] ) ;

def person_or_body($tag):
  ( if ($tag|startswith("1")) then (if $tag=="100" then "person" elif $tag=="110" then "corporate" else "meeting" end)
    else (if $tag=="700" then "person" elif $tag=="710" then "corporate" else "meeting" end) end );
def relator($df): ( [ $df.subfields[]? | (.e? // .4? // empty) ] | first ) // null ;
def name_val($df): ( [ $df.subfields[]? | (.a? // empty), (.b? // empty) ] | map(select(.!=null)) | join(" ") ) // null ;

def creators_block:
  ( ["100","110","111","700","710","711"] | map( dfo(.) | map( . as $df
    | { name: (name_val($df)),
        role: (relator($df) // (if (.[0:3]=="100" or .[0:3]=="700") then "author" else "contributor" end)),
        kind: (person_or_body(.[0:3]))
      } ) ) | add ) as $all
  | [ $all[] | select(.name!=null) ] ;

def identifiers_block:
  (
    [ sf_vals("020";"a")[]?      | {type:"ISBN-13", value: .} ] +
    [ sf_vals("022";"a")[]?      | {type:"ISSN",    value: .} ] +
    [ dfo("024")[]? as $df | ($df.subfields[]? | select(has("a")) | {type: (( $df.subfields[]? | select(has("2")) | .["2"]) // "Other"), value: .a }) ] +
    [ sf_vals("010";"a")[]?      | {type:"LCCN",    value: .} ] +
    [ sf_vals("035";"a")[]?      | {type:"Local",   value: .} ] +
    [ sf_vals("028";"a")[]?      | {type:"MusicPublisher", value: .} ]
  ) ;

def physical_block:
  {
    extent:      (sf1("300";"a")),
    illustrations:(sf1("300";"b")),
    dimensions:  (sf1("300";"c")),
    contentType: (sf1("336";"a")),
    mediaType:   (sf1("337";"a")),
    carrierType: (sf1("338";"a"))
  }
| with_entries(select(.value != null));

def series_block:
  (
    [ dfo("490")[]? | { title: (sf1("490";"a")), numbering: (sf1("490";"v")) } ] +
    [ dfo("830")[]? | { title: (sf1("830";"a")), numbering: (sf1("830";"v")) } ]
  ) | map(with_entries(select(.value != null))) | map(select(length>0));

def subjects_block:
  (
    [ "600","610","611","630","648","650","651","655" ]
    | map( sf_vals(.;"a") ) | add
  ) | unique | map({value:., type:"topical"});

def relations_block:
  { otherFormats: ( [ dfo("776")[]? | [ .subfields[]? | (.z? // .x? // .w? // .t? // empty) ] | map(select(.!=null)) | .[] ] | unique ) } ;

def edition_block: { statement: (sf1("250";"a")), number: (sf1("250";"b")) } | with_entries(select(.value!=null));
def publication_block_full:
  ( publication_block ) as $pb
  | {
    place: $pb.place,
    publisher: $pb.publisher,
    date: $pb.date,
    year: (year_guess),
    countryCode: (
      ( ctrl("008") | first ) as $f008
      | if $f008 and ($f008|length)>=18 then ($f008|.[15:18]) else null end
    )
  } | with_entries(select(.value != null));

def titles_final:
  titles_array
  | map(select(.value != null and (.value|tostring|length)>0))
  | unique;

def source_id:  ( ctrl("001") | first ) // null ;
def source_003: ( ctrl("003") | first ) // null ;

def to_input_record($authorityId; $authorityScheme; $domain; $license; $licenseUri; $signatory; $now; $srcUriPrefix):
  (
    {
      id:        ( if source_id then ("marc:001:" + source_id) else ("marc:tmp:" + ($now|gsub("[^0-9TZ:]";""))) end ),
      provenance: {
        authorityId:   $authorityId,
        authorityScheme: $authorityScheme,
        sourceRecordId: (source_id // "UNKNOWN"),
        sourceRecordUri: ( if ($srcUriPrefix|length)>0 and source_id then ($srcUriPrefix + source_id) else null end ),
        originalFormat: "MARC",
        harvestedAt:    $now
      },
      domain: $domain,
      licenseDeclaration: {
        license: $license,
        licenseUri: (if ($licenseUri|length)>0 then $licenseUri else null end),
        publisherAffirmation: {
          statement: ( ($authorityId + " affirms that it has the rights to publish this metadata and releases it under " + $license) ),
          confirmed: true,
          confirmedAt: $now,
          signatory: (if ($signatory|length)>0 then $signatory else $authorityId end)
        }
      },
      identifiers: identifiers_block,
      titles: titles_final,
      contributors: creators_block,
      languages: languages_block,
      edition: edition_block,
      publication: publication_block_full,
      physical: physical_block,
      subjects: subjects_block,
      series: series_block,
      relations: relations_block,
      classification: (
        [ sf_vals("082";"a")[]? | {scheme:"DDC", value:.} ] +
        [ sf_vals("050";"a")[]? | {scheme:"LCC", value:.} ]
      ),
      notes: (
        [ dfo("500")[]? | {type:"general", value: (sf1("500";"a")) } ] | map(select(.value!=null))
      ),
      rights: {
        rightsStatement: ("Metadata supplied by " + $authorityId),
        license: $license
      },
      admin: {
        created: $now,
        quality: { normalized: false },
        flags: ["ingested"]
      },
      media: {},
      ext: {}
    }
  )
  | walk( if type=="array" then map(select(. != null and . != "")) else . end )
  | with_entries( select(
    ( .value != null ) and
    ( ( (.value|type) != "array" ) or ( (.value|type)=="array" and (.value|length)>0 ) ) and
    ( ( (.value|type) != "object" ) or ( (.value|type)=="object" and (.value|tostring) != "{}" ) )
  ));

to_input_record($authorityId; $authorityScheme; $domain; $license; $licenseUri; $signatory; $now; $srcUriPrefix)
JQ

run_jq() {
  jq \
    --arg authorityId      "$AUTHORITY_ID" \
    --arg authorityScheme  "$AUTHORITY_SCHEME" \
    --arg domain           "$DOMAIN" \
    --arg license          "$LICENSE" \
    --arg licenseUri       "$LICENSE_URI" \
    --arg signatory        "$SIGNATORY" \
    --arg now              "$NOW_ISO" \
    --arg srcUriPrefix     "$SRC_URI_PREFIX" \
    "$JQ_PROG"
}

if [[ $# -eq 0 ]] || ([[ $# -eq 1 && "$1" == "-" ]]); then
  # stdin mode: can be a single JSON, concatenated JSON docs, or NDJSON
  run_jq
else
  # file mode (one output per input)
  for f in "$@"; do
    run_jq < "$f"
  done
fi

