import requests
import sys

userServiceURL = "http://localhost:8080"
bookingServiceURL = "http://localhost:8081"
walletServiceURL = "http://localhost:8082"


# ANSI escape codes for colors
RED = '\033[91m'
RESET = '\033[0m'
GREEN = '\033[92m'

# print = lambda message : print(f"{message} : " + RED + " FAIL" + RESET)
# print = lambda message : print(f"{message} : " + GREEN + "  PASS" + RESET)


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

#Test to get list of all theatres (can be empty list)
def test_get_all_theatres():
    response = get_all_theatres()
    if(response.status_code != 200):
        print("Getting List of all theatres" + RED + " FAIL" + RESET)
    else:
        print("Get all theatres response:", str(str(response.json())) + GREEN + " PASS" + RESET)

#Test to get all shows of an  existing theatre   
def test_get_all_shows_for_theatre_existing():
    theatre_id = 1  # Assuming theatre with ID 1 exists
    response = get_all_shows_for_theatre(theatre_id)
    if(response.status_code != 200):
        print("Failed to get shows for existing theatre" + RED + " FAIL" + RESET)
    else:
        print("Get shows for existing theatre response:", str(response.json()) + GREEN + "  PASS" + RESET)

#Test to get all shows of a non existing theatre
def test_get_all_shows_for_theatre_non_existing():
    theatre_id = 999  # Assuming theatre with ID 999 doesn't exist
    response = get_all_shows_for_theatre(theatre_id)
    if(response.status_code != 404):
        print("Getting shows for non-existing theatre should return 404" + RED + " FAIL" + RESET)
    else:
        print("Get shows for non-existing theatre response:", response.status_code + GREEN + "  PASS" + RESET)

#Test for getting existing show if show exists
def test_get_show_existing():
    show_id = 1  # Assuming show with ID 1 exists
    response = get_show(show_id)
    if(response.status_code != 200):
        print("Failed to get existing show" + RED + " FAIL" + RESET)
    else:
        print("Get existing show response:", str(response.json()) + GREEN + "  PASS" + RESET)

#Test for getting existing show if show doesn't exists
def test_get_show_non_existing():
    show_id = 999  # Assuming show with ID 999 doesn't exist
    response = get_show(show_id)
    if(response.status_code != 404):
        print("Getting non-existing show should return 404" + RED + " FAIL" + RESET)
    else:
         print("Get non-existing show response:", response.status_code + GREEN + "  PASS" + RESET)

#Test to get booking of existing user
def test_get_user_bookings_existing():
    user_id = 1  # Assuming user with ID 1 exists
    response = get_user_bookings(user_id)
    if(response.status_code != 200):
        print("Failed to get existing user's bookings" + RED + " FAIL" + RESET)
    else:
        print("Get existing user's bookings response:", str(response.json()) + GREEN + "  PASS" + RESET)

#Test to get booking of non existing user
def test_get_user_bookings_non_existing():
    user_id = 999  # Assuming user with ID 999 doesn't exist
    response = get_user_bookings(user_id)
    if(response.status_code != 200):
        print("Getting non-existing user's bookings should return empty list" + RED + " FAIL" + RESET)
    else:
        print("Get non-existing user's bookings response:", str(response.json()) + GREEN + "  PASS" + RESET)

#Test to book for seats that are sufficiently available
def test_create_booking_valid():
    show_id = 1  # Assuming show with ID 1 exists
    user_id = 1  # Assuming user with ID 1 exists
    seats_booked = 2  # Assuming there are enough available seats
    response = create_booking(show_id, user_id, seats_booked)
    if(response.status_code != 200):
        print("Failed to create valid booking" + RED + " FAIL" + RESET)
    else:
        print("Create valid booking response:", str(response.json()) + GREEN + "  PASS" + RESET)

#Test to book for show that doesn't exist
def test_create_booking_invalid_show():
    show_id = 999  # Assuming show with ID 999 doesn't exist
    user_id = 1  # Assuming user with ID 1 exists
    seats_booked = 2
    response = create_booking(show_id, user_id, seats_booked)
    if(response.status_code != 400):
        print("Creating Booking with invalid show should return 400" + RED + " FAIL" + RESET)
    else:
        print("Create booking with invalid show response:", str(response.status_code) + GREEN + "  PASS" + RESET)

#Test for booking Show for user that doesn't exist
def test_create_booking_invalid_user():
    show_id = 1  # Assuming show with ID 1 exists
    user_id = 999  # Assuming user with ID 999 doesn't exist
    seats_booked = 2
    response = create_booking(show_id, user_id, seats_booked)
    if(response.status_code != 400):
        print("Creating booking with invalid user should return 400" + RED + " FAIL" + RESET)
    else:
        print("Create booking with invalid user response:", str(response.status_code) + GREEN + "  PASS" + RESET)

#Test for booking seats that are insufficient
def test_create_booking_insufficient_seats():
    show_id = 1  # Assuming show with ID 1 exists
    user_id = 1  # Assuming user with ID 1 exists
    seats_booked = 999  # Assuming there are not enough available seats
    response = create_booking(show_id, user_id, seats_booked)
    if(response.status_code != 400):
        print("Creating booking with insufficient seats should return 400" + RED + " FAIL" + RESET)
    else:
        print("Create booking with insufficient seats response:", str(response.status_code) + GREEN + "  PASS" + RESET)

#Test to delete existing user's bookings
def test_delete_user_bookings_existing():
    user_id = 1  # Assuming user with ID 1 exists
    response = delete_user_bookings(user_id)
    if(response.status_code != 200):
        print("Failed to delete existing user's bookings" + RED + " FAIL" + RESET)
    else:
        print("Delete existing user's bookings response:", str(response.status_code) + GREEN + "  PASS" + RESET)

#Test for deleting bookings for user that doesn't exist
def test_delete_user_bookings_non_existing():
    user_id = 999  # Assuming user with ID 999 doesn't exist
    response = delete_user_bookings(user_id)
    if(response.status_code != 404):
        print("Deleting non-existing user's bookings should return 404" + RED + " FAIL" + RESET)
    else:
        print("Delete non-existing user's bookings response:", str(response.status_code) + GREEN + "  PASS" + RESET)

#Test for deleting shows for existing user
def test_delete_user_show_bookings_existing():
    user_id = 1  # Assuming user with ID 1 exists
    show_id = 1  # Assuming show with ID 1 exists
    response = delete_user_show_bookings(user_id, show_id)
    if(response.status_code != 200):
        print("Failed to delete existing user's bookings for show" + RED + " FAIL" + RESET)
    else:
        print("Delete existing user's bookings for show response:", str(response.status_code) + GREEN + "  PASS" + RESET)

#Test for deleting booking for non existing user
def test_delete_user_show_bookings_non_existing():
    user_id = 999  # Assuming user with ID 999 doesn't exist
    show_id = 999  # Assuming show with ID 999 doesn't exist
    response = delete_user_show_bookings(user_id, show_id)
    if(response.status_code != 404):
        print("Deleting non-existing user's bookings for non-existing show should return 404" + RED + " FAIL" + RESET)
    else:
        print("Delete non-existing user's bookings for non-existing show response:", str(response.status_code) + GREEN + "  PASS" + RESET)

#Test for deleting all bookings
def test_delete_all_bookings():
    response = delete_all_bookings()
    if(response.status_code != 200):
        print("Failed to delete all bookings" + RED + " FAIL" + RESET)
    else:
        print("Delete all bookings response:", str(response.status_code) + GREEN + "  PASS" + RESET)
    

def main():
    test_get_all_theatres()
    test_get_all_shows_for_theatre_existing()
    test_get_all_shows_for_theatre_non_existing()
    test_get_show_existing()
    test_get_show_non_existing()
    test_get_user_bookings_existing()
    test_get_user_bookings_non_existing()
    test_create_booking_valid()
    test_create_booking_invalid_show()
    test_create_booking_invalid_user()
    test_create_booking_insufficient_seats()
    test_delete_user_bookings_existing()
    test_delete_user_bookings_non_existing()
    test_delete_user_show_bookings_existing()
    test_delete_user_show_bookings_non_existing()
    test_delete_all_bookings()




if __name__ == "__main__":
    main()