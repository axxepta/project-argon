(:~ Path to resource. :)
declare variable $PATH as xs:string external;

let $db := if(contains($PATH, '/')) then substring-before($PATH, '/') else $PATH
let $path := substring-after($PATH, '/')
return if(string-length($path) = 0) then (
(: drop database :)
db:drop($db)
) else (
    db:delete($db, $path)
)