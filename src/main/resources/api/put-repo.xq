(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ Resource (XML string or Base64). :)
declare variable $RESOURCE as xs:string external;
(:~ Put copy in history and increase revision? :)
declare variable $VERSIONIZE as xs:boolean external;
(: increase version? :)
declare variable $VERSION-UP as xs:boolean external;

let $path := db:system()//repopath || '/' || $PATH
return if(starts-with($RESOURCE, '<')) then (
file:write-text($path, $RESOURCE)
) else (
file:write-binary($path, xs:base64Binary($RESOURCE))
)