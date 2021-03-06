(:~ Root path for search. :)
declare variable $PATH as xs:string external;
(:~ Search filter. :)
declare variable $FILTER as xs:string external;

let $system := db:system()
let $webpath := $system//webpath/string()
(: absolute, or relative to webpath :)
let $restxqpath := $system//restxqpath/string()

let $path := file:resolve-path($restxqpath, file:resolve-path($webpath)) || $PATH
for $resource in file:list($path, true())
let $dir := file:is-dir($resource)
(: show directories first, case-insensitive order :)
order by $dir descending, lower-case($resource)
return (
    $resource
)