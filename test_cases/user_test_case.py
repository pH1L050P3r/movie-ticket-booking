import requests
import sys

userServiceURL = "http://localhost:8080"
bookingServiceURL = "http://localhost:8081"
walletServiceURL = "http://localhost:8082"


# ANSI escape codes for colors
RED = '\033[91m'
RESET = '\033[0m'
GREEN = '\u001b[32m'



name = "Alice"
email = "alice@example.com"

USER_SECOND = {
    "name" : "Bod",
    "email" : "bob@example.com"
}


############################################# USER END POINTS ###########################################
#Create User
def create_user(name, email):
    delete_all_users()
    new_user = {"name": name, "email": email}
    response = requests.post(userServiceURL + "/users", json=new_user)
    return response

#Delete User
def delete_all_users():
    requests.delete(userServiceURL+f"/users") 

#Get User
def get_user(user_id):
    response = requests.get(userServiceURL + "/users/" + str(user_id))
    return response

################################################# TESTING ##############################################

# Test case for creating a new user with valid data
def test_create_user():
    delete_all_users()
    response_valid = create_user(name, email)
    if(response_valid.status_code != 201):
        print(RED + "Failed to create user with valid data" + RESET)
    else:
        print("Status Code :", response_valid.status_code)
        print("Valid user creation response:", response_valid.json())


# Test case for creating ag user with an existing email
def test_existing_email():
    response_existing_email = create_user(name, email)
    if(response_existing_email.status_code == 400):
        print(RED + "Creating user with existing email should return 400" + RESET)
    else:
        print("User creation with existing email response:", response_existing_email.status_code)


# Test case for creating a user with missing name
def test_missing_name():
    delete_all_users()
    email_missing_name = "bob@example.com"
    response_missing_name = create_user("", email_missing_name)
    if(response_missing_name.status_code == 400):
        print(RED + "Creating user with missing name should return 400" + RESET)
    else:
        print("User creation with missing name response:", response_missing_name.status_code)


# Test case for creating a user with missing email
def test_missing_email():
    delete_all_users()
    name_missing_email = "Bob"
    response_missing_email = create_user(name_missing_email, "")
    if(response_missing_email.status_code != 400):
        print(RED + "Creating user with missing email should return 400" + RESET)
    else:
        print("User creation with missing email response:", response_missing_email.status_code)

def check_email_format():
    delete_all_users()
    # Test case for valid Email format
    name = "Bob"
    email = "invalid_email"
    response = create_user(name, email)
    if(response.status_code != 400):
        print(RED + "Creating user with invalid email format should return 400" + RESET)
    else:
        print("User creation with invalid email format response:", response.status_code)
    
def test_missing_name_and_email():
    delete_all_users()
    # Test case for empty name and email
    name = ""
    email = ""
    response = create_user(name, email)
    if(response.status_code != 400):
        print(RED + "Creating user with empty name and email should return 400" + RESET)
    else:
         print("User creation with empty name and email response:", response.status_code)

def test_get_user_existing():
    delete_all_users()
    user = create_user(USER_SECOND.get("name"), USER_SECOND.get("email"))
    if(user.status_code != 201):
        print(RED + "Failed to get existing user" + RESET)

    response = get_user(user.json().get("id"))
    if(response.status_code != 200):
        print(RED + "Failed to get existing user" + RESET)
    else:
       print("Get existing user response:", response.json())

########################################################################################################
    
def main():
    test_create_user()
    test_existing_email()
    test_missing_name()
    test_missing_email()
    check_email_format()
    test_missing_name_and_email()
    test_get_user_existing()    

if __name__ == "__main__":
    main()