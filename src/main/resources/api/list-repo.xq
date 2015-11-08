(:~ Path to resource. :)
declare variable $PATH as xs:string external;

let $path := db:system()//repopath || '/' || $PATH
for $resource in file:children($path)
let $name := file:name($resource)
let $dir := file:is-dir($resource)
(: show directories first, case-insensitive order :)
order by $dir descending, lower-case($name)

return (
    if($dir) then 'directory' else 'resource',
    (: file:size($resource) :)
    $name
)