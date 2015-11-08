(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ Resource (XML string or Base64). :)
declare variable $RESOURCE as xs:string external;

let $path := db:system()//repopath || '/' || $PATH
return if(starts-with($RESOURCE, '<')) then (
file:write-text($path, $RESOURCE)
) else (
file:write-binary($path, xs:base64Binary($RESOURCE))
)