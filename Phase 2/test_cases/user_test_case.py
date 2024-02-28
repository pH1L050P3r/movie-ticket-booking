import requests

userServiceURL = "http://localhost:8080"
bookingServiceURL = "http://localhost:8081"
walletServiceURL = "http://localhost:8082"


# ANSI escape codes for colors
RED = '\033[91m'
RESET = '\033[0m'
GREEN = '\u001b[32m'

USER_FIRST = {
    "name" : "user1",
    "email" : "user1@example.com"
}

USER_SECOND = {
    "name" : "user2",
    "email" : "user2@example.com"
}

fail_print = lambda message : print(f"{message} : " + RED + "FAIL" + RESET)
pass_print = lambda message : print(f"{message} : " + GREEN + "PASS" + RESET)


class TestCaseRunner():
    def __init__(self):
        self.test_cases = []
        self.userServiceURL = "http://localhost:8080"
        self.bookingServiceURL = "http://localhost:8081"
        self.walletServiceURL = "http://localhost:8082"
    
    def run(self):
        self.setUp()
        for func in self.test_cases:
            self.fixture(func)

    def test(self, func):
        self.test_cases.append(func)
    
    def setUp(self):
        pass
    
    def fixture(self, func):
        # delete all user before every test case
        requests.delete(userServiceURL+f"/users") 
        func()

test_runner = TestCaseRunner()

############################################# USER END POINTS ###########################################
#Create User
def create_user(name, email):
    return requests.post(userServiceURL + "/users", json={"name": name, "email": email})


#Delete Users
def delete_all_users():
    return requests.delete(userServiceURL+f"/users") 


#Delete User
def delete_user(user_id):
    return requests.delete(userServiceURL + "/users/" + str(user_id))


#Get User
def get_user(user_id):
    return requests.get(userServiceURL + "/users/" + str(user_id))

#Get Wallet
def get_wallet(walletId):
    return requests.get(walletServiceURL + "/wallets/" + str(walletId))


################################################# TESTING ##############################################

# Test case for creating a new user with valid data
@test_runner.test
def test_create_user():
    response_valid = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    if(response_valid.status_code != 201):
        fail_print("Test Create User")
    else:
        pass_print("Test Create User")

@test_runner.test
def test_create_user_response_format():
    response_valid = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    if(response_valid.status_code != 201):
        fail_print("Test Create User response format")
        return
    for key in ["id", "name", "email"]:
        if key not in response_valid.json().keys():
            fail_print("Test Create User response format")
            return
    
    pass_print("Test Create User response format")


# Test case for creating ag user with an existing email
@test_runner.test
def test_existing_email():
    response_user = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    response_existing_email = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))

    if(response_existing_email.status_code != 400):
        fail_print("Test Create User with existing email")
    else:
        pass_print("Test Create User with existing email")


# Test case for creating a user with missing name
@test_runner.test
def test_missing_name():
    email_missing_name = "bob@example.com"
    response_missing_name = create_user("", email_missing_name)
    if(response_missing_name.status_code == 400):
        pass_print("Test Create User with missing name")
    else:
        fail_print("Test Create User with missing name")


# Test case for creating a user with missing email
@test_runner.test
def test_missing_email():
    name_missing_email = "Bob"
    response_missing_email = create_user(name_missing_email, "")
    if(response_missing_email.status_code != 400):
        fail_print("Test Create User with missing email")
    else:
        pass_print("Test Create User with missing email")


@test_runner.test
def test_check_email_format():
    # Test case for valid Email format
    name = "Bob"
    email = "invalid_email"
    response = create_user(name, email)
    if(response.status_code != 400):
        fail_print("Test Create User with invalid email")
    else:
        pass_print("Test Create User with invalid email")


@test_runner.test
def test_get_existing_user():
    user = create_user(USER_SECOND.get("name"), USER_SECOND.get("email"))

    response = get_user(user.json().get("id"))
    if(response.status_code != 200):
        fail_print("Test Get existing user")
    else:
        pass_print("Test Get existing user")
    

@test_runner.test
def test_get_user_response_format():
    response_valid = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    response_valid = get_user(response_valid.json().get("id"))
    if(response_valid.status_code != 200):
        fail_print("Test Create User response format")
        return
    for key in ["id", "name", "email"]:
        if key not in response_valid.json().keys():
            fail_print("Test Get User response format")
            return
    
    pass_print("Test Get User response format")


@test_runner.test
def test_delete_user():
    response_user = create_user(USER_SECOND.get("name"), USER_SECOND.get("email"))
    delete_user(response_user.json().get("id"))
    response_existing_user = get_user(response_user.json().get("id"))
    response_wallet = get_wallet(response_user.json().get("id"))
    if(response_existing_user.status_code == 200 or response_wallet.status_code == 200):
        fail_print("Test delete user")
    else:
        pass_print("Test delete user")


@test_runner.test
def test_delete_all_users():
    user_first = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    user_second = create_user(USER_SECOND.get("name"), USER_SECOND.get("email"))
    delete_all_users()
    response_first_user = get_user(user_first.json().get("id"))
    response_second_user = get_user(user_second.json().get("id"))

    if(response_first_user.status_code == 404 or response_second_user.status_code == 404):
        pass_print("Test delete all user")
    else:
        fail_print("Test delete all user")


########################################################################################################


if __name__ == "__main__":
    try:
        test_runner.run()
    except Exception as e:
        print("Something went wrong")