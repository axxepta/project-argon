(:~ Path to resource. :)
declare variable $PATH as xs:string external;

let $repopath := if (empty(db:system()//repopath)) then (
    error(xs:QName("api"), "Need admin rights to access repo path.")
) else (db:system()//repopath)

let $path := db:system()//repopath || '/' || $PATH
for $resource in file:list($path, true())
let $name := concat($path, '/', $resource)
let $dir := file:is-dir($name)
(: show directories first, case-insensitive order :)
order by $dir descending, lower-case($name)

return (
    if($dir) then 'directory' else 'resource',
    (: file:size($resource) :)
    concat($PATH, '/', $resource)
)