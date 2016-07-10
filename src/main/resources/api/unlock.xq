(:~ Source. :)
declare variable $SOURCE as xs:string external;
(:~ Path to resource. :)
declare variable $PATH as xs:string external;

(:~ Lock database. :)
declare variable $LOCK-DB := '~argon';
declare variable $USER-FILE := '~usermanagement';

let $user := user:current()
let $exists := db:exists($LOCK-DB, $USER-FILE)
let $locks := (
    if($exists)
    then db:open($LOCK-DB, $USER-FILE)
    else document { <usermanagement><locks/><groups><group name="admin"><user>admin</user></group></groups></usermanagement> }
) update (
    delete node *//locks/*[name() = $SOURCE][text() = $PATH][@user = $user]
)
return if($exists) then (
    db:replace($LOCK-DB, $USER-FILE, $locks)
) else (
    db:create($LOCK-DB, $locks, $USER-FILE)
)