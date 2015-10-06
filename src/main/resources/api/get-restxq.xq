declare option output:method "raw";

(:~ Path to resource. :)
declare variable $PATH as xs:string external;

let $system := db:system()
let $webpath := $system//webpath/string()
let $restxqpath := $system//restxqpath/string()
let $path := file:resolve-path($restxqpath, file:resolve-path($webpath)) || $PATH

return if(file:exists($path)) then (
    file:read-binary($path)
) else (
(: raise error if resource does not exist :)
error(xs:QName("api"), "RESTXQ resource does not exist: " || $PATH)
)