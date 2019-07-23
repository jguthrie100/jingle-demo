# jingle-demo

# TODO

* Fix error messages when trying to save duplicate UNIQUE values in repository 
* Convert to using modules
* Finish tests

# Role Management Implementation

I would create a repository containing containing different User roles, with either a one-to-many relationship with Users,
 or a many-to-many relationship (using a _middleman_, join table). One User could then have many roles.
Each Role class could also contain a list of methods which the User is permitted to do.
 
Roles would then inherit from each other, so a ```Guest``` role would have a certain subset of permitted operations. ```Member``` could then inherit 
from ```Guest``` and implement a further set of methods. ```Admin``` could then inherit from ```Member``` and so on.

Before running any operations, a check would be made to see what roles the user has/belongs to, and whether the current operation is available to them.
This could be as simple as saving a list of permitted operations in a repository/class and checking against it every time an operation is made.

For example:

    delete(long id) {
      if(currUser.getRole().allowedToDelete() == true) {
        //delete
      } else {
        // throw Exception
      }
    }
    
 Alternatively, each role could be an interface which reveals all it's available methods.
 An admin might implement ```public void deleteUser(long id)``` with the full functionality, whereas the Guest role implements delete, but it just 
 immediately throws an Exception.
 
 A member might implement both ```public void deleteUser(long id)``` _and_ ```public void deleteUser(long id, String authkey)```, with the first method throwing an exception 
 and the second one actually deleting (but requiring the authKey).
  
     delete(long id) {
       try {
         currUser.getRole().operations().delete(id);
       } catch (UnauthorisedRoleException e) {
         // Handle
       }
     }
  
  This could then be extended further with whole subsets of methods only being available to the relevant Roles.
