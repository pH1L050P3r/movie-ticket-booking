#delete users and check if all bookings and wallet corresponding to that user is deleted.

import requests

userServiceURL = "http://localhost:8080"
bookingServiceURL = "http://localhost:8081"
walletServiceURL = "http://localhost:8082"

def main():
	name = "John Cena"
	email = "johncena@mail.com"
	name2="Ravi Kumar"
	email2="ravi@mail.com"
	if(delete_users_and_check_wallets_and_booking(name,email,name2,email2)):
		print("Test passed")
	else:
		print("Test failed")
	
	

def create_user(name, email):
    new_user = {"name": name, "email": email}
    response = requests.post(userServiceURL + "/users", json=new_user)
    return response



def get_wallet(user_id):
	response=requests.get(walletServiceURL + f"/wallets/{user_id}")
	return response

def get_booking(user_id):
    response = requests.get(walletServiceURL + f"/bookings/users/{user_id}")
    return response


def update_wallet(user_id,action,amount):
	response=requests.put(walletServiceURL + f"/wallets/{user_id}", json={"action":action, "amount":amount})
	return response
    

def delete_users():
    requests.delete(userServiceURL+f"/users")  

def delete_user(user_id):
    requests.delete(userServiceURL+f"/users/{user_id}") 

def get_show_details(show_id):
    response = requests.get(bookingServiceURL + f"/shows/{show_id}")
    return response 
    
def delete_users_and_check_wallets_and_booking(name,email,name2,email2):
	try:
		delete_users()
		new_user = create_user(name,email)
		new_userid = new_user.json()['id']
		new_user2=create_user(name2,email2)
		new_userid2 = new_user2.json()['id']
		update_wallet(new_userid,"credit",100)
		update_wallet(new_userid2,"credit",200)
		
		new_booking = {"show_id": 5, "user_id": new_userid, "seats_booked": 1}
		response=requests.post(bookingServiceURL + "/bookings", json=new_booking)
		new_booking2 = {"show_id": 10, "user_id": new_userid2, "seats_booked": 1}
		response2=requests.post(bookingServiceURL + "/bookings", json=new_booking2)

		
		delete_user(new_userid)
		bookings_by_user=get_booking(new_userid)
		# bookings_by_user_json=bookings_by_user.json()
		wallet_of_user=get_wallet(new_userid)
		# wallet_of_user_json=wallet_of_user.json()

		

		if(bookings_by_user.status_code!=404 or wallet_of_user.status_code!=404):
			return False
		 			
		return True

			
	except Exception as e:
		print(e.with_traceback())
		print("Some Exception occurred ")


if __name__ == "__main__":
    main()

