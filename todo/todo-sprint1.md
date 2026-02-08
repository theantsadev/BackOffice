* Sprint 1

TL : mike , 3327
Dev1 : Vicky , 3254 -> FO
Dev2 :  Jordi , 3351 -> BO

### Hotel
 - id (PK)
 - nom

### Reservation 
 - id (PK)
 - id_client (4 chiffres) varchar (saisissena fotsiny , pas de table)
 - nb_passager int 
 - date_heure_arrivee datetime
 - id_hotel (FK)


BackOffice (meme base)
 - besoin de formulaire reservation (sans protection)
   affichage : 
    -  creer page de formulaire reservation (
    id_client saisie libre [input] , 
    nb_passager [input] , 
    date_heure_arrivee [selecteur de date ], 
    hotel [liste deroulante -> appel de getAllHotel()] , 
    bouton reserver -> appel metier reserver (id_client, nb_passager , date_heure_arrivee , id_hotel) )

   metier : 
    - Class Reservation 
       -  Reservation   reserver (id_client, nb_passager , date_heure_arrivee , id_hotel) -> INSERT Reservation
       - List<Reservation> getAllReservation() -> SELECT * Reservation
       - List<Reservation> getReservationByDate(Date date)

    - Class Hotel
       - List<Hotel> getAllHotel() -> SELECT *  Hotel

    api REST: 
     GET /reservations : getAllReservation()
     POST /reservations : reserver (id_client, nb_passager , date_heure_arrivee , id_hotel)
     GET /reservations : @RequestParam Date date  getReservationByDate(Date date)

     GET /hotels : getAllHotel()


 - script d'insertion de hotel (initialisation) 
    

FrontOffice (meme base) (spring mvc , miantso api)
 - liste reservation + recherche par date (pas datetime) 
   affichage 
   - creer page misy tableau de liste reservation 
   - pour chaque reservation , on affiche les infos

   integration
   - appel api JSON sprint 9 de getAllReservation() 
   - appel api JSON de getReservationByDate(Date date) 