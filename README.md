# jingle-demo

# Install
Pull the git repo, and then in the root directory run `mvn package`, followed by `java -jar target/demo-0.0.1-SNAPSHOT.jar`

# How to use

Point a REST client to the base url: `https://localhost:8443` and use the following end points.

___POST /signup___ - requires 'username', 'firstname, 'lastname', 'email' and 'password' - returns new user.)

___POST /login___ - requires 'username' and 'password' - returns authentication key.
    
___PUT /edit___ - requires 'userid' and 'authkey', with all other params being optional - returns updated user.
    
___DELETE /delete___ - requires 'userid' and 'authkey' - returns whether successful or not
    
___GET /user___ - requires either 'userid' or 'username' - returns user

# Role Management Implementation

I would create a repository containing containing different Roles, with either a one-to-many relationship with Users,
 or a many-to-many relationship (using a _middleman_ join table, allowing a user to have many roles.)
Each Role class could contain a list of methods which the User is permitted to do.
 
Roles could then inherit from each other, so a ```Guest``` role would have a certain subset of permitted operations. ```Member``` could then inherit all the methods from ```Guest``` and implement a further set of methods. ```Admin``` could then inherit all the methods from from ```Member``` and so on.

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


# TODO

* Convert to using modules
I've converted the functionality to modules ( www.github.com/jguthrie/jingle-mod ), but can't get it to run quite properly.
