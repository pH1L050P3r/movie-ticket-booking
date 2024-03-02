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
        userServiceURL = "http://localhost:8080"
        bookingServiceURL = "http://localhost:8081"
        walletServiceURL = "http://localhost:8082"

    
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


############################################# END POINTS ###########################################
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

def put_wallet(walletId, amount, action):
    return requests.put(walletServiceURL + "/wallets/" + str(walletId), json={"amount" : amount, "action" : action})

def delete_wallet(walletId):
    return requests.delete(walletServiceURL + "/wallets/" + str(walletId))

def delete_all_wallets():
    return requests.delete(walletServiceURL + "/wallets")


################################################# TESTING ##############################################

@test_runner.test
def test_wallet_created_while_user_created():
    response_user = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    response_wallet = put_wallet(response_user.json().get("id"), 0, "credit")
    response_wallet = get_wallet(response_user.json().get("id"))

    if response_wallet.status_code != 200:
        fail_print("Test Wallet Created while Creating User")
    else:
        pass_print("Test Wallet Created while Creating User")


@test_runner.test
def test_wallet_update_credit_balance():
    amount = 20000
    response_user = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    response_wallet = put_wallet(response_user.json().get("id"), amount, "credit")
    response_wallet = get_wallet(response_user.json().get("id"))

    if response_wallet.status_code != 200 or response_wallet.json().get("balance") != 20000:
        fail_print("Test Wallet Balance Credit Amount Update")
    else:
        pass_print("Test Wallet Balance Credit Amount Update")

@test_runner.test
def test_wallet_update_debit_balance():
    amount = 20000
    debit_amount = 2000
    response_user = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    response_wallet = put_wallet(response_user.json().get("id"), amount, "credit")
    response_wallet = put_wallet(response_user.json().get("id"), debit_amount, "debit")
    response_wallet = get_wallet(response_user.json().get("id"))

    if response_wallet.status_code != 200 or response_wallet.json().get("balance") != (20000 - 2000):
        fail_print("Test Wallet Balance Debit Amount Update")
    else:
        pass_print("Test Wallet Balance Debit Amount Update")


@test_runner.test
def test_wallet_update_with_negative_balance():
    amount = -20000
    response_user = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    response_wallet = put_wallet(response_user.json().get("id"), amount, "credit")

    if response_wallet.status_code != 400:
        fail_print("Test Wallet Balance Update with negative amount")
    else:
        pass_print("Test Wallet Balance Update with negative amount")

@test_runner.test
def test_wallet_update_debit_from_insufficient_balance():
    amount = 20000
    response_user = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    response_wallet = put_wallet(response_user.json().get("id"), amount, "debit")

    if response_wallet.status_code != 400:
        fail_print("Test Wallet Balance Update while balance is Insufficient")
    else:
        pass_print("Test Wallet Balance Update while balance is Insufficient")

@test_runner.test
def test_wallet_delete():
    response_user = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    response_wallet = put_wallet(response_user.json().get("id"), 0, "credit")
    response_delete = delete_wallet(response_user.json().get("id"))
    response_get_delete = get_wallet(response_user.json().get("id"))

    if response_delete.status_code != 200 or response_get_delete.status_code != 404:
        fail_print("Test Wallet Delete")
    else:
        pass_print("Test Wallet Delete")


@test_runner.test
def test_wallet_all_delete():
    response_user_first = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    response_user_second = create_user(USER_SECOND.get("name"), USER_SECOND.get("email"))
    response_delete = delete_all_wallets()
    response_get_delete_first = get_wallet(response_user_first.json().get("id"))
    response_get_delete_second = get_wallet(response_user_second.json().get("id"))

    if response_delete.status_code != 200 or response_get_delete_first.status_code != 404 or response_get_delete_second.status_code != 404:
        fail_print("Test Wallet All Delete")
    else:
        pass_print("Test Wallet All Delete")


##########################################################################################################

if __name__ == "__main__":
    try:
        test_runner.run()
    except Exception as e:
        print(e.with_traceback())
        print("Something went wrong")