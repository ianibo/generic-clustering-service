#!/bin/bash

rm -Rf cs00000002m0*                              
count=0
while IFS= read -r line; do
  ((count++))
  echo process $count
  # echo $line > te1
  echo $line | sed 's/\\\\/\\/g' > t1
  recid=`cat t1 | jq '.header.identifier'`
  src_and_id=`echo $recid | cut -f 3 -d: | sed 's/\//_/' | sed 's/"\$//'`
  src=`echo $src_and_id | cut -f1 -d_`
  id=`echo $src_and_id | cut -f2 -d_`
  if [ ! -d "$src" ]; then
    mkdir $src
  fi
  echo $src $id
  cat t1 | jq '.metadata' | jq -e -f marc2gcs.jq \
        --arg authorityId GCSTEST \
        --arg authorityScheme GCSTEST \
        --arg domain bibliographic \
        --arg license CC0-1.0 \
        --arg licenseUri https://creativecommons.org/publicdomain/zero/1.0/ \
        --arg signatory "CGS Metadata Services" \
        --arg now "$(date -u +"%Y-%m-%dT%H:%M:%SZ")" \
        --arg srcUriPrefix https://catalog.example.org/record/ \
        > $src/$id

done < ../faust-records.json
