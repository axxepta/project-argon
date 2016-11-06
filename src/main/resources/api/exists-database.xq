(:~ Path to resource. :)
declare variable $PATH as xs:string external;

let $db := if(contains($PATH, '/')) then substring-before($PATH, '/') else $PATH
let $path := substring-after($PATH, '/')

return db:exists($db, $path)