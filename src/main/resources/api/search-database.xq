(: name of database :)
let $db := if(contains($xpath, '/')) then substring-before($xpath, '/') else $xpath
(: path: ensure existence trailing slash :)
let $path := replace(substring-after($xpath, '/'), '([^/])$', '$1/')
(: retrieve all entries on this and lower levels :)
let $resources := db:list($db, $path)

return (
    $resources
)