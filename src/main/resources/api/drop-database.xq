(:~ New database name. :)
declare variable $DB as xs:string external;

let $exists := db:exists($DB)
let $meta := concat('~meta_', $DB)
let $history := concat('~history_', $DB)

return if(not($exists)) then (
) else (
    db:drop($DB),
    db:drop($meta),
    db:drop($history)
)