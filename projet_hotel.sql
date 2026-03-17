--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5
-- Dumped by pg_dump version 17.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: distance; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.distance (
    id_distance integer NOT NULL,
    from_hotel integer NOT NULL,
    to_hotel integer NOT NULL,
    valeur double precision NOT NULL,
    CONSTRAINT check_from_lt_to CHECK ((from_hotel < to_hotel))
);


ALTER TABLE public.distance OWNER TO postgres;

--
-- Name: distance_id_distance_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.distance_id_distance_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.distance_id_distance_seq OWNER TO postgres;

--
-- Name: distance_id_distance_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.distance_id_distance_seq OWNED BY public.distance.id_distance;


--
-- Name: hotel; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.hotel (
    id_hotel integer NOT NULL,
    nom character varying(50)
);


ALTER TABLE public.hotel OWNER TO postgres;

--
-- Name: hotel_id_hotel_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.hotel_id_hotel_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.hotel_id_hotel_seq OWNER TO postgres;

--
-- Name: hotel_id_hotel_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.hotel_id_hotel_seq OWNED BY public.hotel.id_hotel;


--
-- Name: parametre; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.parametre (
    id_parametre integer NOT NULL,
    cle character varying(100) NOT NULL,
    valeur double precision NOT NULL,
    unite character varying(50)
);


ALTER TABLE public.parametre OWNER TO postgres;

--
-- Name: parametre_id_parametre_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.parametre_id_parametre_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.parametre_id_parametre_seq OWNER TO postgres;

--
-- Name: parametre_id_parametre_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.parametre_id_parametre_seq OWNED BY public.parametre.id_parametre;


--
-- Name: planification; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.planification (
    id_planification integer NOT NULL,
    id_reservation integer NOT NULL,
    id_vehicule integer NOT NULL,
    date_heure_depart_aeroport timestamp without time zone NOT NULL,
    date_heure_retour_aeroport timestamp without time zone NOT NULL
);


ALTER TABLE public.planification OWNER TO postgres;

--
-- Name: planification_id_planification_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.planification_id_planification_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.planification_id_planification_seq OWNER TO postgres;

--
-- Name: planification_id_planification_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.planification_id_planification_seq OWNED BY public.planification.id_planification;


--
-- Name: reservation; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reservation (
    id_reservation integer NOT NULL,
    nb_passager integer,
    date_heure_arrivee timestamp without time zone,
    id_client character varying(50),
    id_hotel integer NOT NULL
);


ALTER TABLE public.reservation OWNER TO postgres;

--
-- Name: reservation_id_reservation_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.reservation_id_reservation_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.reservation_id_reservation_seq OWNER TO postgres;

--
-- Name: reservation_id_reservation_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.reservation_id_reservation_seq OWNED BY public.reservation.id_reservation;


--
-- Name: token; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.token (
    id integer NOT NULL,
    token character varying(255) NOT NULL,
    date_heure_expiration timestamp without time zone NOT NULL
);


ALTER TABLE public.token OWNER TO postgres;

--
-- Name: token_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.token_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.token_id_seq OWNER TO postgres;

--
-- Name: token_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.token_id_seq OWNED BY public.token.id;


--
-- Name: vehicule; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.vehicule (
    id integer NOT NULL,
    reference character varying(50) NOT NULL,
    place integer NOT NULL,
    type_carburant character(1) NOT NULL,
    CONSTRAINT vehicule_type_carburant_check CHECK ((type_carburant = ANY (ARRAY['D'::bpchar, 'E'::bpchar])))
);


ALTER TABLE public.vehicule OWNER TO postgres;

--
-- Name: vehicule_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.vehicule_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.vehicule_id_seq OWNER TO postgres;

--
-- Name: vehicule_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.vehicule_id_seq OWNED BY public.vehicule.id;


--
-- Name: distance id_distance; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distance ALTER COLUMN id_distance SET DEFAULT nextval('public.distance_id_distance_seq'::regclass);


--
-- Name: hotel id_hotel; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hotel ALTER COLUMN id_hotel SET DEFAULT nextval('public.hotel_id_hotel_seq'::regclass);


--
-- Name: parametre id_parametre; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parametre ALTER COLUMN id_parametre SET DEFAULT nextval('public.parametre_id_parametre_seq'::regclass);


--
-- Name: planification id_planification; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.planification ALTER COLUMN id_planification SET DEFAULT nextval('public.planification_id_planification_seq'::regclass);


--
-- Name: reservation id_reservation; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservation ALTER COLUMN id_reservation SET DEFAULT nextval('public.reservation_id_reservation_seq'::regclass);


--
-- Name: token id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.token ALTER COLUMN id SET DEFAULT nextval('public.token_id_seq'::regclass);


--
-- Name: vehicule id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicule ALTER COLUMN id SET DEFAULT nextval('public.vehicule_id_seq'::regclass);


--
-- Data for Name: distance; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.distance (id_distance, from_hotel, to_hotel, valeur) FROM stdin;
2	0	2	10
3	0	3	8
4	0	4	20
5	1	2	5
6	1	3	7
7	1	4	12
8	2	3	4
9	2	4	15
10	3	4	18
11	0	5	8
12	3	5	3
1	0	1	50
\.


--
-- Data for Name: hotel; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.hotel (id_hotel, nom) FROM stdin;
0	Aeroport
1	Colbert
2	Novotel
3	Ibis
4	Lokanga
5	Gare
\.


--
-- Data for Name: parametre; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.parametre (id_parametre, cle, valeur, unite) FROM stdin;
2	temps_attente_min	30	min
1	vitesse_moyenne_kmh	50	km/h
\.


--
-- Data for Name: planification; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.planification (id_planification, id_reservation, id_vehicule, date_heure_depart_aeroport, date_heure_retour_aeroport) FROM stdin;
\.


--
-- Data for Name: reservation; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.reservation (id_reservation, nb_passager, date_heure_arrivee, id_client, id_hotel) FROM stdin;
27	7	2026-03-12 09:00:00	Client1	1
28	11	2026-03-12 09:00:00	Client2	1
29	3	2026-03-12 09:00:00	Client3	1
30	1	2026-03-12 09:00:00	Client4	1
31	2	2026-03-12 09:00:00	Client5	1
32	20	2026-03-12 09:00:00	Client6	1
\.


--
-- Data for Name: token; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.token (id, token, date_heure_expiration) FROM stdin;
1	e954b214-28c9-405a-ac42-35ce34e9edd7	2026-03-12 07:03:31.745
2	11d44eb4-693a-4f43-befd-b07f2c9de12a	2026-03-12 10:23:06.075
3	2bb12fc2-4129-47ee-bcca-c4424709b0bf	2026-03-12 16:08:49.466
4	e536ef42-252a-4dda-9ac9-4ea8333d3c31	2026-03-12 21:29:32.698
5	13acdaf2-4527-4aa8-991f-99120855547d	2026-03-12 22:32:29.771
6	d6299103-51bf-41fe-a4c4-83560f7303dd	2026-03-17 12:22:32.864
\.


--
-- Data for Name: vehicule; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.vehicule (id, reference, place, type_carburant) FROM stdin;
1	VH-001	12	D
2	VH-002	5	E
3	VH-003	5	D
4	VH-004	12	E
\.


--
-- Name: distance_id_distance_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.distance_id_distance_seq', 12, true);


--
-- Name: hotel_id_hotel_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.hotel_id_hotel_seq', 1, false);


--
-- Name: parametre_id_parametre_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.parametre_id_parametre_seq', 2, true);


--
-- Name: planification_id_planification_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.planification_id_planification_seq', 78, true);


--
-- Name: reservation_id_reservation_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.reservation_id_reservation_seq', 32, true);


--
-- Name: token_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.token_id_seq', 6, true);


--
-- Name: vehicule_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.vehicule_id_seq', 6, true);


--
-- Name: distance distance_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distance
    ADD CONSTRAINT distance_pkey PRIMARY KEY (id_distance);


--
-- Name: hotel hotel_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.hotel
    ADD CONSTRAINT hotel_pkey PRIMARY KEY (id_hotel);


--
-- Name: parametre parametre_cle_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parametre
    ADD CONSTRAINT parametre_cle_key UNIQUE (cle);


--
-- Name: parametre parametre_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.parametre
    ADD CONSTRAINT parametre_pkey PRIMARY KEY (id_parametre);


--
-- Name: planification planification_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.planification
    ADD CONSTRAINT planification_pkey PRIMARY KEY (id_planification);


--
-- Name: reservation reservation_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT reservation_pkey PRIMARY KEY (id_reservation);


--
-- Name: token token_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.token
    ADD CONSTRAINT token_pkey PRIMARY KEY (id);


--
-- Name: token token_token_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.token
    ADD CONSTRAINT token_token_key UNIQUE (token);


--
-- Name: distance unique_distance; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distance
    ADD CONSTRAINT unique_distance UNIQUE (from_hotel, to_hotel);


--
-- Name: vehicule vehicule_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicule
    ADD CONSTRAINT vehicule_pkey PRIMARY KEY (id);


--
-- Name: vehicule vehicule_reference_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vehicule
    ADD CONSTRAINT vehicule_reference_key UNIQUE (reference);


--
-- Name: distance fk_from_hotel; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distance
    ADD CONSTRAINT fk_from_hotel FOREIGN KEY (from_hotel) REFERENCES public.hotel(id_hotel);


--
-- Name: planification fk_reservation; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.planification
    ADD CONSTRAINT fk_reservation FOREIGN KEY (id_reservation) REFERENCES public.reservation(id_reservation);


--
-- Name: distance fk_to_hotel; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.distance
    ADD CONSTRAINT fk_to_hotel FOREIGN KEY (to_hotel) REFERENCES public.hotel(id_hotel);


--
-- Name: planification fk_vehicule; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.planification
    ADD CONSTRAINT fk_vehicule FOREIGN KEY (id_vehicule) REFERENCES public.vehicule(id);


--
-- Name: reservation reservation_id_hotel_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservation
    ADD CONSTRAINT reservation_id_hotel_fkey FOREIGN KEY (id_hotel) REFERENCES public.hotel(id_hotel);


--
-- PostgreSQL database dump complete
--

