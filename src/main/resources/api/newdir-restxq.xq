(:~ Path to new directory. :)
declare variable $PATH as xs:string external;

let $system := db:system()
let $webpath := $system//webpath/string()
let $restxqpath := if (empty($system//restxqpath)) then (
    error(xs:QName("api"), "Need admin rights to access query path.")
) else ($system//restxqpath/string())

let $path := file:resolve-path($restxqpath, file:resolve-path($webpath)) || $PATH
let $dir-exists := file:exists($path)
return if($dir-exists) then () else(file:create-dir($path))