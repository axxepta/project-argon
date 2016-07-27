(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ Resource (XML string or Base64). :)
declare variable $RESOURCE as xs:string external;
(:~ Put copy in history and increase revision? :)
declare variable $VERSIONIZE as xs:boolean external;
(: increase version? :)
declare variable $VERSION-UP as xs:boolean external;

let $pathtokens := tokenize($PATH, '/')
let $ntokens := count($pathtokens)
let $dir := if($ntokens gt 1) then (
    let $tokenlengths := for $i in (1 to ($ntokens -1)) return string-length(subsequence($pathtokens, $i, 1))
    let $dirEnd := sum($tokenlengths) + $ntokens - 2
    return concat(db:system()//repopath || '/' , substring($PATH, 1, $dirEnd))
) else (
   db:system()//repopath || ''
)

let $path := db:system()//repopath || '/' || $PATH
let $dir-exists := file:exists($dir)
return if(starts-with($RESOURCE, '<')) then (
    if($dir-exists) then () else(file:create-dir($dir)),
    file:write-text($path, $RESOURCE)
) else (
    if($dir-exists) then () else(file:create-dir($dir)),
    file:write-binary($path, xs:base64Binary($RESOURCE))
)