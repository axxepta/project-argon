(:~ Lock database. :)
declare variable $LOCK-DB := '~argon';
declare variable $USER-FILE := '~usermanagement';

let $user := user:current()
let $lock-file := db:open($LOCK-DB, $USER-FILE)

for $lock in $lock-file//locks/*[@user = $user]
return (
    $lock/name(), $lock/text()
)