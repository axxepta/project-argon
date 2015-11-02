(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ New path to resource. :)
declare variable $NEWPATH as xs:string external;

let $system := db:system()
let $webpath := $system//webpath/string()
let $restxqpath := $system//restxqpath/string()
let $path := file:resolve-path($restxqpath, file:resolve-path($webpath)) || $PATH
let $newpath := file:resolve-path($restxqpath, file:resolve-path($webpath)) || $NEWPATH

file:move($path, newpath)
