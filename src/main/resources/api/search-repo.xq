(:~ Root path for search. :)
declare variable $PATH as xs:string external;
(:~ Search filter. :)
declare variable $FILTER as xs:string external;

let $path := db:system()//repopath || '/' || $xpath
for $resource in file:list($path, true())
let $dir := file:is-dir($resource)
(: show directories first, case-insensitive order :)
order by $dir descending, lower-case($resource)

return (
    $resource
)
