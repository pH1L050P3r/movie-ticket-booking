import requests
import random
import sys
from http import HTTPStatus
from threading import Thread

from user import *
from booking import *
from wallet import *

debited_amount = 0
credited_amount = 0

SHOWS = [0, 1, 2, 3, 4, 5, 6 ,7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]
INITIAL_SEATS = [0, 40,30,55,65,50,40,60,55,45,65,40,50,60,45,55,65,40,50,60,45]
CREATE_SEATS = [0] * (len(SHOWS) + 1)
DELETE_SEATS = [0] * (len(SHOWS) + 1)




# Thread 1: Credits into user's wallet
def t1(user_id):
    global SHOWS, INITIAL_SEATS, CREATE_SEATS
    for i in range(200):
        show = random.randint(1, len(SHOWS))
        seats = random.randint(1, 20)
        payload = {"show_id":show, "user_id":user_id, "seats_booked":seats}
        response = requests.post(bookingServiceURL + "/bookings", json = payload)
        if response.status_code == 200:
            CREATE_SEATS[show] += seats

# Thread 2: Debits from user's wallet
def t2(user_id):
    global SHOWS, INITIAL_SEATS, CREATE_SEATS
    for i in range(200):
        show_id = random.randint(1, 20)
        response = requests.delete(bookingServiceURL+f"/bookings/users/{user_id}/shows/{show_id}")
        if response.status_code == 200:
            CREATE_SEATS[show_id] = 0 

def main():
    try:
        global INITIAL_SEATS
        for i in SHOWS[1:]:
            show = get_show(i)
            if(show.status_code != 200):
                print_fail_message("Error while getting Show with Id : {}".format(i))
                return
            INITIAL_SEATS.append(show.json()['seats_available'])

        response = delete_users()
        if not check_response_status_code(response, 200):
            return False
    
        response = delete_wallets()
        if not check_response_status_code(response, 200):
            return False


        response1 = post_user('Anurag Kumar1','ak47@iisc.ac.in')
        if not test_post_user('Anurag Kumar1','ak47@iisc.ac.in', response1):
            return False
        user1 = response1.json()
        initial_balance = 100000
        response1 = put_wallet(user1['id'], 'credit', initial_balance)
        if not test_put_wallet(user1['id'], 'credit', initial_balance, 0, response1):
            return False
        

        response2 = post_user('Anurag Kumar2','ak48@iisc.ac.in')
        if not test_post_user('Anurag Kumar2','ak48@iisc.ac.in', response2):
            return False
        user2 = response2.json()
        initial_balance = 100000
        response2 = put_wallet(user2['id'], 'credit', initial_balance)
        if not test_put_wallet(user2['id'], 'credit', initial_balance, 0, response2):
            return False
        

        response3 = post_user('Anurag Kumar3','ak49@iisc.ac.in')
        if not test_post_user('Anurag Kumar3','ak49@iisc.ac.in', response3):
            return False
        user3 = response3.json()
        initial_balance = 100000
        response3 = put_wallet(user3['id'], 'credit', initial_balance)
        if not test_put_wallet(user3['id'], 'credit', initial_balance, 0, response3):
            return False
        

        response4 = post_user('Anurag Kumar4','ak50@iisc.ac.in')
        if not test_post_user('Anurag Kumar4','ak50@iisc.ac.in', response4):
            return False
        user4 = response4.json()
        initial_balance = 100000
        response4 = put_wallet(user4['id'], 'credit', initial_balance)
        if not test_put_wallet(user4['id'], 'credit', initial_balance, 0, response4):
            return False
        

        ### Parallel Execution Begins ###
        thread1 = Thread(target=t1, kwargs = {'user_id': user1['id']})
        thread2 = Thread(target=t1, kwargs = {'user_id': user2['id']})
        thread3 = Thread(target=t1, kwargs = {'user_id': user3['id']})
        thread4 = Thread(target=t1, kwargs = {'user_id': user4['id']})
        # thread2 = Thread(target=t2, kwargs = {'user_id': user['id']})

        thread1.start()
        thread2.start()
        thread3.start()
        thread4.start()

        thread1.join()
        thread2.join()
        thread3.join()
        thread4.join()
        ### Parallel Execution Ends ###

        # response = get_wallet(user['id'])
        # if not test_get_wallet(user['id'], response, True, initial_balance + credited_amount - debited_amount):
        #     return False
        
        for i in SHOWS[1:]:
            show = get_show(i)
        
        
        for i in SHOWS[1:]:
            show = get_show(i)
            if(show.status_code != 200):
                print_fail_message("Error while getting Show with Id : {}".format(i))
                return
            if(show.json()["seats_available"] != (INITIAL_SEATS[i] - CREATE_SEATS[i])):
                print_fail_message("Incorrect seats_available value getting {} but required {}".format(INITIAL_SEATS[i] - CREATE_SEATS[i], show.json()["seats_available"]))
                return
            print_pass_message("Correct seats_available value getting {} but required {}".format(INITIAL_SEATS[i] - CREATE_SEATS[i], show.json()["seats_available"]))
        return True
    except:
        return False

if __name__ == "__main__":
    if main():
        sys.exit(0)
    else:
        sys.exit(1)
