(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ Resource (XML string or Base64). :)
declare variable $RESOURCE as xs:string external;
(:~ Binary storage? :)
declare variable $BINARY as xs:string external;
(:~ Original encoding for XML. Irrelevant for RestXQ. :)
declare variable $ENCODING as xs:string external;
(:~ Put copy in history and increase revision? :)
declare variable $VERSIONIZE as xs:boolean external;
(: increase version? :)
declare variable $VERSION-UP as xs:boolean external;

let $system := db:system()
let $webpath := $system//webpath/string()
let $restxqpath := if (empty($system//restxqpath)) then (
    error(xs:QName("api"), "Need admin rights to access query path.")
) else ($system//restxqpath/string())

let $pathtokens := tokenize($PATH, '/')
let $ntokens := count($pathtokens)
let $dir := if($ntokens gt 1) then (
    let $tokenlengths := for $i in (1 to ($ntokens -1)) return string-length(subsequence($pathtokens, $i, 1))
    let $dirEnd := sum($tokenlengths) + $ntokens - 2
    return concat(file:resolve-path($restxqpath, file:resolve-path($webpath)) ||  '/' , substring($PATH, 1, $dirEnd))
) else (
   file:resolve-path($restxqpath, file:resolve-path($webpath)) || ''
)

let $path := file:resolve-path($restxqpath, file:resolve-path($webpath)) || $PATH
let $dir-exists := file:exists($dir)
return if(compare($BINARY, 'true') = 0) then (
    if($dir-exists) then () else(file:create-dir($dir)),
    file:write-text($path, $RESOURCE)
) else (
    if($dir-exists) then () else(file:create-dir($dir)),
    file:write-binary($path, xs:base64Binary($RESOURCE))
)