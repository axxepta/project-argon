(:~ New database name. :)
declare variable $DB as xs:string external;

let $exists := db:exists($DB)
return if(not($exists)) then (
    db:create($DB)
) else ()
