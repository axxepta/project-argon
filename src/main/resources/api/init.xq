(:~ Source. :)
declare variable $RESOURCE as xs:string external;

declare variable $LOCK-DB := '~argon';
declare variable $REPO_HIST := '~history_~repo';
declare variable $XQ_HIST := '~history_~restxq';
declare variable $PATH := 'MetaTemplate.xml';

let $exists := db:exists($LOCK-DB)
let $xq_exists := db:exists($XQ_HIST)
let $repo_exists := db:exists($REPO_HIST)

return (
if($exists) then ( db:replace($LOCK-DB, $PATH, $RESOURCE) ) else ( db:create($LOCK-DB, $RESOURCE, $PATH) ),
if($xq_exists) then () else (db:create($XQ_HIST)),
if($repo_exists) then () else (db:create($REPO_HIST))
)