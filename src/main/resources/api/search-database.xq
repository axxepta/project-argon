(:~ Root path for search. :)
declare variable $PATH as xs:string external;
(:~ Search filter. :)
declare variable $FILTER as xs:string external;

(: name of database :)
let $db := if(contains($PATH, '/')) then substring-before($PATH, '/') else $PATH
(: path: ensure existence trailing slash :)
let $path := replace(substring-after($PATH, '/'), '([^/])$', '$1/')
(: retrieve all entries on this and lower levels :)
let $resources := db:list($db, $path)

return (
    $resources
)