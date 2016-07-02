(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ Resource (XML string or Base64). :)
declare variable $RESOURCE as xs:string external;
(:~ Put copy in history and increase revision? :)
declare variable $VERSIONIZE as xs:boolean external;
(: increase version? :)
declare variable $VERSION-UP as xs:boolean external;

let $system := db:system()
let $webpath := $system//webpath/string()
let $restxqpath := $system//restxqpath/string()
let $path := file:resolve-path($restxqpath, file:resolve-path($webpath)) || $PATH
return if(starts-with($RESOURCE, '<')) then (
    file:write-text($path, $RESOURCE)
) else (
    file:write-binary($path, xs:base64Binary($RESOURCE))
)