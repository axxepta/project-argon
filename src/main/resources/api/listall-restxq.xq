(:~ Path to resource. :)
declare variable $PATH as xs:string external;

let $system := db:system()
let $webpath := $system//webpath/string()
(: absolute, or relative to webpath :)
let $restxqpath := if (empty($system//restxqpath)) then (
    error(xs:QName("api"), "Need admin rights to access query path.")
) else ($system//restxqpath/string())

let $path := file:resolve-path($restxqpath, file:resolve-path($webpath)) || $PATH
for $resource in file:list($path, true())
let $name := concat($path, '/', $resource)
let $dir := file:is-dir($name)
(: show directories first, case-insensitive order :)
order by $dir descending, lower-case($name)
return (
    if($dir) then 'directory' else 'resource',
    concat($PATH, '/', $resource)
)