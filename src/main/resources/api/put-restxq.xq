(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ Resource. :)
declare variable $RESOURCE as xs:base64Binary external;

let $system := db:system()
let $webpath := $system//webpath/string()
let $restxqpath := $system//restxqpath/string()
let $path := file:resolve-path($restxqpath, file:resolve-path($webpath)) || $PATH
return file:write-binary($path, $RESOURCE)