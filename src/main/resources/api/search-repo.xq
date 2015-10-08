let $path := db:system()//repopath || '/' || $xpath
for $resource in file:list($path, true())
let $dir := file:is-dir($resource)
(: show directories first, case-insensitive order :)
order by $dir descending, lower-case($resource)

return (
    $resource
)
