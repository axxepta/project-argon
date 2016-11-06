(:~ Root path for search. :)
declare variable $PATH as xs:string external;
(:~ Search filter. :)
declare variable $FILTER as xs:string external;
(:~ Whole word. :)
declare variable $WHOLE as xs:string external;
(:~ Exact Case. :)
declare variable $EXACTCASE as xs:string external;

let $whole := ($WHOLE eq 'true')
let $exactcase := ($EXACTCASE eq 'true')

(: name of database :)
let $db := if(contains($PATH, '/')) then substring-before($PATH, '/') else $PATH
(: path: ensure existence trailing slash :)
let $path := replace(substring-after($PATH, '/'), '([^/])$', '$1/')

let $resources := if (empty($path)) then (
db:list($db)
) else (
db:list($db, $path)
)
for $resource in $resources
let $conn := db:open($db, $resource)

return if ($whole) then (
    if ($exactcase) then (
        if ($conn//*[attribute() = $FILTER]) then ($resource) else ()
    ) else (
        if ($conn//@*[lower-case(.) = lower-case($FILTER)]) then ($resource) else ()
    )
) else (
    if ($exactcase) then (
        if ($conn//*/@*[contains(., $FILTER)]) then ($resource) else ()
    ) else (
        if ($conn//*/@*[contains(lower-case(.), lower-case($FILTER))]) then ($resource) else ()
    )
)