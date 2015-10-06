declare option output:method "raw";

(:~ Path to resource. :)
declare variable $PATH as xs:string external;

let $db := if(contains($PATH, '/')) then substring-before($PATH, '/') else $PATH
let $path := substring-after($PATH, '/')
let $exists := db:exists($db, $path)
return if($exists and db:is-xml($db, $path)) then (
    serialize(db:open($db, $path))
) else if($exists and db:is-raw($db, $path)) then (
    db:retrieve($db, $path)
) else (
(: raise error if database or resource does not exist :)
error(xs:QName("api"), "Database resource does not exist: " || $PATH)
)