# ===== marc2gcs.jq =====

# Coerce top-level: decode stringified JSON if present; then ensure marc4j shape
def ensure_marc:
  if (type=="object" and has("record") and (.record|has("fields"))) then .
  else ("Input is not a marc4j object with .record.fields" | halt_error(2))
  end;

( if type=="string" then (fromjson? // .) else . end | ensure_marc ) as $R |

# ---------- Helpers over marc4j (always read from $R) ----------
def fields: $R.record.fields;
def ctrl($tag): [ fields[] | select(has($tag)) | .[$tag] | select(type=="string") ];
def dfo($tag):  [ fields[] | select(has($tag)) | .[$tag] | select(type=="object") ];
def sfs($t):    [ dfo($t)[]?.subfields[]? ];
def sf_vals($t;$c): [ sfs($t)[] | select(has($c)) | .[$c] ];
def sf1($t;$c):
  (sf_vals($t;$c)) as $v
  | if ($v|length)>0 then $v[0] else null end;

# ---------- Title extraction ----------
def title_main:
  (sf1("245";"a")) as $a |
  (sf1("245";"b")) as $b |
  if $a!=null and $b!=null then ($a + " " + $b)
  else (if $a!=null then $a else $b end) end;

def title_uniform:
  (sf1("240";"a")) as $u |
  if $u!=null then $u else sf1("130";"a") end;

def titles_array:
  (
    (if title_main!=null then [{value:title_main, type:"main"}] else [] end)
    +
    (if title_uniform!=null then [{value:title_uniform, type:"uniform"}] else [] end)
    +
    ([ sf_vals("246";"a")[]? | {value:., type:"alternative"} ])
  );

# ---------- Creators / contributors ----------
def creators_block:
  (
    ["100","110","111","700","710","711"]
    | map( . as $tag
      | dfo($tag)
      | map(
        . as $df
        | ( [ $df.subfields[]? | .a?, .b? ] | map(select(.!=null)) | join(" ") ) as $nm
        | ( [ $df.subfields[]? | .e?, .["4"]? ] | map(select(.!=null)) | (if length>0 then .[0] else null end) ) as $role
        | {
          name: $nm,
          role: ( if $role!=null then $role
                  else ( if ($tag=="100" or $tag=="700") then "author" else "contributor" end )
                end ),
          kind: ( if ($tag=="100" or $tag=="700") then "person"
                  elif ($tag=="110" or $tag=="710") then "corporate"
                  else "meeting" end )
          }
      )
    )
    | add
  ) as $all
  | [ $all[] | select(.name!=null and .name!="") ];

# ---------- Identifiers ----------
def identifiers_block:
  (
    [ sf_vals("020";"a")[]? | {type:"ISBN", value:.} ] +
    [ sf_vals("022";"a")[]? | {type:"ISSN", value:.} ] +
    [ dfo("024")[]? as $df
      | ( $df.subfields[]? | select(has("a"))
        | ( [ $df.subfields[]? | .["2"]? ] | map(select(.!=null)) | (if length>0 then .[0] else "Other" end) ) as $ty
        | { type: $ty, value: .a } )
    ] +
    [ sf_vals("010";"a")[]? | {type:"LCCN", value:.} ] +
    [ sf_vals("035";"a")[]? | {type:"Local", value:.} ] +
    [ sf_vals("028";"a")[]? | {type:"MusicPublisher", value:.} ]
  );

# ---------- Languages (prefer 041$a, else 008[35:38]) ----------
def languages_block:
  ( sf_vals("041";"a") | map(tostring) | unique ) as $langs
  | if ($langs|length)>0 then $langs
    else (
      (ctrl("008")|first) as $f008
      | if ($f008!=null and ($f008|length)>=38) then [ ($f008|.[35:38]) ] else [] end
    ) end;

# ---------- Publication ----------
def pub_field($tag; $ind2):
  ( [ dfo($tag)[]? | select( ($ind2==null) or (.ind2==$ind2) ) ] | (if length>0 then .[0] else null end) );

def publication_block:
  (pub_field("264"; "1")) as $p1 |
  (if $p1!=null then $p1 else pub_field("260"; null) end) as $p |
  {
    place:     ( [ $p.subfields[]? | .a? ] | map(select(.!=null)|tostring) | unique ),
    publisher: ( [ $p.subfields[]? | .b? ] | map(select(.!=null)|tostring) | unique ),
    date:      ( [ $p.subfields[]? | .c? ] | map(select(.!=null)) | (if length>0 then .[0] else null end) )
  };

# Year from 264/260$c (first 4-digit) else 008[7:11]
def year_guess:
  ( publication_block.date | tostring ) as $c |
  ( if ($c!=null) then ( ($c | capture("(?<y>[0-9]{4})") | .y) ) else null end ) as $y1 |
  if $y1!=null then ($y1|tonumber)
  else (
    ( ctrl("008") | first ) as $f008
    | if $f008!=null and ($f008|length)>=11 then (($f008|tostring|.[7:11])|tonumber) else null end
  ) end;

def publication_block_full:
  ( publication_block ) as $pb
  | {
    place: $pb.place,
    publisher: $pb.publisher,
    date: $pb.date,
    year: (year_guess),
    countryCode: (
      ( ctrl("008") | first ) as $f008
      | if $f008!=null and ($f008|length)>=18 then ($f008|.[15:18]) else null end
    )
  }
  | with_entries(select(
    (.value!=null) and
    ( ( (.value|type)!="array") or ((.value|length)>0) )
  ));

# ---------- Physical ----------
def physical_block:
  {
    extent: sf1("300";"a"),
    illustrations: sf1("300";"b"),
    dimensions: sf1("300";"c"),
    contentType: sf1("336";"a"),
    mediaType: sf1("337";"a"),
    carrierType: sf1("338";"a")
  } | with_entries(select(.value!=null));

# ---------- Subjects, Series, Relations ----------
def subjects_block:
  ( [ "600","610","611","630","648","650","651","655" ]
    | map( sf_vals(.;"a") ) | add ) as $vals
  | if $vals==null then [] else ($vals|unique|map({value:., type:"topical"})) end;

def series_block:
  (
    [ dfo("490")[]? | {title:sf1("490";"a"), numbering:sf1("490";"v")} ] +
    [ dfo("830")[]? | {title:sf1("830";"a"), numbering:sf1("830";"v")} ]
  )
  | map(with_entries(select(.value!=null)))
  | map(select(length>0));

def relations_block:
  { otherFormats:
    ( [ dfo("776")[]?
      | [ .subfields[]? | .z?, .x?, .w?, .t? ]
      | map(select(.!=null)) | .[]
      ] | unique )
  };

# ---------- Source ID ----------
def source_id:
  (ctrl("001")) as $v | if ($v|length)>0 then $v[0] else null end;

# ---------- Prune (drop nulls, empty arrays/objects) ----------
def prune:
  if type=="object" then
    with_entries( .value |= prune )
    | with_entries( select(.value!=null
                           and ( (.value|type)!="array"  or ((.value|length)>0))
                           and ( (.value|type)!="object" or ((.value|length)>0)) ) )
  elif type=="array" then
    map(prune)
    | map(select(.!=null
                 and ( (type!="array") or (length>0) )
                 and ( (type!="object") or (length>0) )))
  else . end;

# ---------- Entry point ----------
def to_input_record($authorityId; $authorityScheme; $domain; $license; $licenseUri; $signatory; $now; $srcUriPrefix):
  {
    id: (
      source_id as $sid
      | if $sid!=null then ("marc:001:"+$sid)
        else ("marc:tmp:" + ($now|gsub("[^0-9TZ:]";"")))
        end
    ),
    provenance: {
      authorityId: $authorityId,
      authorityScheme: $authorityScheme,
      sourceRecordId: ( if source_id!=null then source_id else "UNKNOWN" end ),
      sourceRecordUri: ( if (($srcUriPrefix|length)>0 and source_id!=null) then ($srcUriPrefix + source_id) else null end ),
      originalFormat: "MARC",
      harvestedAt: $now
    },
    domain: $domain,
    licenseDeclaration: {
      license: $license,
      licenseUri: ( if ($licenseUri|length)>0 then $licenseUri else null end ),
      publisherAffirmation: {
        statement: ($authorityId + " affirms that it has the rights to publish this metadata and releases it under " + $license),
        confirmed: true,
        confirmedAt: $now,
        signatory: ( if ($signatory|length)>0 then $signatory else $authorityId end )
      }
    },
    identifiers: identifiers_block,
    titles: (titles_array | map(select(.value!=null and (.value|tostring|length)>0)) | unique),
    contributors: creators_block,
    languages: languages_block,
    edition: ({ statement: sf1("250";"a"), number: sf1("250";"b") } | with_entries(select(.value!=null))),
    publication: publication_block_full,
    physical: physical_block,
    subjects: subjects_block,
    series: series_block,
    relations: relations_block
  } | prune;

to_input_record($authorityId; $authorityScheme; $domain; $license; $licenseUri; $signatory; $now; $srcUriPrefix)

# ===== end =====

