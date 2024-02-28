import requests
import sys

userServiceURL = "http://localhost:8080"
bookingServiceURL = "http://localhost:8081"
walletServiceURL = "http://localhost:8082"


# userServiceURL = "http://10.217.54.237:8080"
# bookingServiceURL = "http://10.217.54.237:8081"
# walletServiceURL = "http://10.217.54.237:8082"

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


#Create User
def create_user(name, email):
    return requests.post(userServiceURL + "/users", json={"name": name, "email": email})


def put_wallet(walletId, amount, action):
    return requests.put(walletServiceURL + "/wallets/" + str(walletId), json={"amount" : amount, "action" : action})



###################################################################### BOOKING SERVICE #############################################################

def get_all_theatres():
    return requests.get(bookingServiceURL + "/theatres")

def get_all_shows_for_theatre(theatre_id):
    return requests.get(bookingServiceURL + f"/shows/theatres/{theatre_id}")

def get_show(show_id):
    return requests.get(bookingServiceURL + f"/shows/{show_id}")

def get_user_bookings(user_id):
    return requests.get(bookingServiceURL + f"/bookings/users/{user_id}")

def create_booking(show_id, user_id, seats_booked):
    payload = {"show_id": show_id, "user_id": user_id, "seats_booked": seats_booked}
    response = requests.post(bookingServiceURL + "/bookings", json=payload)
    return response

def delete_user_bookings(user_id):
    response = requests.delete(bookingServiceURL + f"/bookings/users/{user_id}")
    return response

def delete_user_show_bookings(user_id, show_id):
    response = requests.delete(bookingServiceURL + f"/bookings/users/{user_id}/shows/{show_id}")
    return response

def delete_all_bookings():
    response = requests.delete(bookingServiceURL + "/bookings")
    return response


##################################################################### TESTING #####################################################################

@test_runner.test
def test_create_user():
    response_valid = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    if(response_valid.status_code != 201):
        fail_print("Test Create User")
    else:
        pass_print("Test Create User")

#Test to get list of all theatres (can be empty list)
@test_runner.test
def test_get_all_theatres():
    response = get_all_theatres()
    if(response.status_code != 200):
        fail_print("Getting List of all theatres")
    else:
        pass_print("Get all theatres")

#Test to get all shows of an  existing theatre   
@test_runner.test
def test_get_all_shows_for_theatre_existing():
    theatre_id = 1  # Assuming theatre with ID 1 exists
    response = get_all_shows_for_theatre(theatre_id)
    if(response.status_code != 200):
        fail_print("Failed to get shows for existing theatre")
    else:
        pass_print("Get shows for existing theatre")

#Test to get all shows of a non existing theatre
@test_runner.test
def test_get_all_shows_for_theatre_non_existing():
    theatre_id = 999  # Assuming theatre with ID 999 doesn't exist
    response = get_all_shows_for_theatre(theatre_id)
    if(response.status_code != 404):
        fail_print("Getting shows for non-existing theatre should return 404")
    else:
        pass_print("Get shows for non-existing theatre")

#Test for getting existing show if show exists
@test_runner.test
def test_get_show_existing():
    show_id = 1  # Assuming show with ID 1 exists
    response = get_show(show_id)
    if(response.status_code != 200):
        fail_print("Failed to get existing show")
    else:
        pass_print("Get existing show")

#Test for getting existing show if show doesn't exists
@test_runner.test
def test_get_show_non_existing():
    show_id = 999  # Assuming show with ID 999 doesn't exist
    response = get_show(show_id)
    if(response.status_code != 404):
        fail_print("Getting non-existing show should return 404")
    else:
         pass_print("Get non-existing show")

#Test to get booking of existing user
def test_get_user_bookings_existing():
    user_id = 1  # Assuming user with ID 1 exists
    response = get_user_bookings(user_id)
    if(response.status_code != 200):
        fail_print("Failed to get existing user's bookings")
    else:
        pass_print("Get existing user's bookings ")

#Test to get booking of non existing user
@test_runner.test
def test_get_user_bookings_non_existing():
    user_id = 999  # Assuming user with ID 999 doesn't exist
    response = get_user_bookings(user_id)
    if(response.status_code != 200):
        print("Getting non-existing user's bookings should return empty list" + RED + " FAIL" + RESET)
    else:
        print("Get non-existing user's bookings " + GREEN + "  PASS" + RESET)

#Test to book for seats that are sufficiently available
@test_runner.test
def test_create_booking_valid():
    user_response = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    update_wallet = put_wallet(user_response.json().get("id"), 50000, "credit")
    show_id = 1  # Assuming show with ID 1 exists
    user_id = user_response.json().get("id")
    seats_booked = 2  # Assuming there are enough available seats
    response = create_booking(show_id, user_id, seats_booked)
    if(response.status_code != 200):
        print("Failed to create valid booking" + RED + " FAIL" + RESET)
    else:
        print("Create valid booking " + GREEN + "  PASS" + RESET)

#Test to book for show that doesn't exist
@test_runner.test
def test_create_booking_invalid_show():
    show_id = 999  # Assuming show with ID 999 doesn't exist
    user_response = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    user_id = user_response.json().get("id")
    seats_booked = 2
    response = create_booking(show_id, user_id, seats_booked)
    if(response.status_code != 400):
        fail_print("Creating Booking with invalid show should return 400")
    else:
        pass_print("Create booking with invalid show ")

#Test for booking Show for user that doesn't exist
@test_runner.test
def test_create_booking_invalid_user():
    show_id = 1  # Assuming show with ID 1 exists
    user_id = 999  # Assuming user with ID 999 doesn't exist
    seats_booked = 2
    response = create_booking(show_id, user_id, seats_booked)
    if(response.status_code != 400):
        fail_print("Creating booking with invalid user should return 400")
    else:
        pass_print("Create booking with invalid user")

#Test for booking seats that are insufficient
@test_runner.test
def test_create_booking_insufficient_seats():
    show_id = 1  # Assuming show with ID 1 exists
    user_response = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    user_id = user_response.json().get("id")
    seats_booked = 999  # Assuming there are not enough available seats
    response = create_booking(show_id, user_id, seats_booked)
    if(response.status_code != 400):
        fail_print("Creating booking with insufficient seats should return 400")
    else:
        pass_print("Create booking with insufficient seats")

#Test to delete existing user's bookings
@test_runner.test
def test_delete_user_bookings_existing():
    user_response = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    update_wallet = put_wallet(user_response.json().get("id"), 50000, "credit")
    show_id = 1  # Assuming show with ID 1 exists
    user_id = user_response.json().get("id")
    seats_booked = 2  # Assuming there are enough available seats
    response = create_booking(show_id, user_id, seats_booked)
    response = delete_user_bookings(user_id)
    if(response.status_code != 200):
        fail_print("Failed to delete existing user's bookings")
    else:
        pass_print("Delete existing user's bookings")

#Test for deleting bookings for user that doesn't exist
@test_runner.test
def test_delete_user_bookings_non_existing():
    user_id = 999  # Assuming user with ID 999 doesn't exist
    response = delete_user_bookings(user_id)
    if(response.status_code != 404):
        fail_print("Deleting non-existing user's bookings should return 404")
    else:
        pass_print("Delete non-existing user's bookings")

#Test for deleting shows for existing user
@test_runner.test
def test_delete_user_show_bookings_existing():
    user_response = create_user(USER_FIRST.get("name"), USER_FIRST.get("email"))
    update_wallet = put_wallet(user_response.json().get("id"), 50000, "credit")
    show_id = 1  # Assuming show with ID 1 exists
    user_id = user_response.json().get("id")
    seats_booked = 2  # Assuming there are enough available seats
    response = create_booking(show_id, user_id, seats_booked)
    response = delete_user_show_bookings(user_id, show_id)
    if(response.status_code != 200):
        fail_print("Failed to delete existing user's bookings for show")
    else:
        pass_print("Delete existing user's bookings for show")

#Test for deleting booking for non existing user
@test_runner.test
def test_delete_user_show_bookings_non_existing():
    user_id = 999  # Assuming user with ID 999 doesn't exist
    show_id = 999  # Assuming show with ID 999 doesn't exist
    response = delete_user_show_bookings(user_id, show_id)
    if(response.status_code != 404):
        fail_print("Deleting non-existing user's bookings for non-existing show should return 404")
    else:
        pass_print("Delete non-existing user's bookings for non-existing show")

#Test for deleting all bookings
@test_runner.test
def test_delete_all_bookings():
    response = delete_all_bookings()
    if(response.status_code != 200):
        fail_print("Delete all bookings")
    else:
        pass_print("Delete all bookings")


if __name__ == "__main__":
    try:
        test_runner.run()
    except Exception as e:
        print("Something went wrong")