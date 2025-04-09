-- Create Admins table
CREATE TABLE IF NOT EXISTS Admins (
                                      AdminID INTEGER PRIMARY KEY AUTOINCREMENT,
                                      Username TEXT UNIQUE NOT NULL,
                                      Password TEXT NOT NULL
);

-- add Admin credentials
INSERT OR IGNORE INTO Admins (Username, Password) VALUES
                                                      ('ib', 'ib'),
                                                      ('admin', 'admin');

-- Create Guests table
CREATE TABLE IF NOT EXISTS Guests (
                                      GuestID INTEGER PRIMARY KEY AUTOINCREMENT,
                                      Name TEXT NOT NULL,
                                      PhoneNumber TEXT NOT NULL UNIQUE,
                                      Email TEXT NOT NULL UNIQUE
);

-- Create RoomTypes table
CREATE TABLE IF NOT EXISTS RoomTypes (
                                         RoomType TEXT PRIMARY KEY CHECK(RoomType IN ('SINGLE', 'DOUBLE', 'DELUXE', 'PENTHOUSE')),
                                         NumberOfBeds INTEGER NOT NULL,
                                         Price REAL NOT NULL
);

-- Insert default room types
INSERT OR IGNORE INTO RoomTypes (RoomType, NumberOfBeds, Price) VALUES
                                                                    ('SINGLE', 2, 450.0),
                                                                    ('DOUBLE', 4, 700.0),
                                                                    ('DELUXE', 2, 1200.0),
                                                                    ('PENTHOUSE', 2, 1500.0);

-- Create Rooms table
CREATE TABLE IF NOT EXISTS Rooms (
                                     RoomID INTEGER PRIMARY KEY AUTOINCREMENT,
                                     RoomType TEXT NOT NULL CHECK(RoomType IN ('SINGLE', 'DOUBLE', 'DELUXE', 'PENTHOUSE')),
                                     FOREIGN KEY (RoomType) REFERENCES RoomTypes(RoomType)
);

-- 35 SINGLE rooms
INSERT OR REPLACE INTO Rooms (RoomID, RoomType) VALUES
                                                    (1, 'SINGLE'), (2, 'SINGLE'), (3, 'SINGLE'), (4, 'SINGLE'), (5, 'SINGLE'),
                                                    (6, 'SINGLE'), (7, 'SINGLE'), (8, 'SINGLE'), (9, 'SINGLE'), (10, 'SINGLE'),
                                                    (11, 'SINGLE'), (12, 'SINGLE'), (13, 'SINGLE'), (14, 'SINGLE'), (15, 'SINGLE'),
                                                    (16, 'SINGLE'), (17, 'SINGLE'), (18, 'SINGLE'), (19, 'SINGLE'), (20, 'SINGLE'),
                                                    (21, 'SINGLE'), (22, 'SINGLE'), (23, 'SINGLE'), (24, 'SINGLE'), (25, 'SINGLE'),
                                                    (26, 'SINGLE'), (27, 'SINGLE'), (28, 'SINGLE'), (29, 'SINGLE'), (30, 'SINGLE'),
                                                    (31, 'SINGLE'), (32, 'SINGLE'), (33, 'SINGLE'), (34, 'SINGLE'), (35, 'SINGLE');

-- 50 DOUBLE rooms
INSERT OR REPLACE INTO Rooms (RoomID, RoomType) VALUES
                                                    (36, 'DOUBLE'), (37, 'DOUBLE'), (38, 'DOUBLE'), (39, 'DOUBLE'), (40, 'DOUBLE'),
                                                    (41, 'DOUBLE'), (42, 'DOUBLE'), (43, 'DOUBLE'), (44, 'DOUBLE'), (45, 'DOUBLE'),
                                                    (46, 'DOUBLE'), (47, 'DOUBLE'), (48, 'DOUBLE'), (49, 'DOUBLE'), (50, 'DOUBLE'),
                                                    (51, 'DOUBLE'), (52, 'DOUBLE'), (53, 'DOUBLE'), (54, 'DOUBLE'), (55, 'DOUBLE'),
                                                    (56, 'DOUBLE'), (57, 'DOUBLE'), (58, 'DOUBLE'), (59, 'DOUBLE'), (60, 'DOUBLE'),
                                                    (61, 'DOUBLE'), (62, 'DOUBLE'), (63, 'DOUBLE'), (64, 'DOUBLE'), (65, 'DOUBLE'),
                                                    (66, 'DOUBLE'), (67, 'DOUBLE'), (68, 'DOUBLE'), (69, 'DOUBLE'), (70, 'DOUBLE'),
                                                    (71, 'DOUBLE'), (72, 'DOUBLE'), (73, 'DOUBLE'), (74, 'DOUBLE'), (75, 'DOUBLE'),
                                                    (76, 'DOUBLE'), (77, 'DOUBLE'), (78, 'DOUBLE'), (79, 'DOUBLE'), (80, 'DOUBLE'),
                                                    (81, 'DOUBLE'), (82, 'DOUBLE'), (83, 'DOUBLE'), (84, 'DOUBLE'), (85, 'DOUBLE');

-- 10 DELUXE rooms (RoomID: 86 to 95)
INSERT OR REPLACE INTO Rooms (RoomID, RoomType) VALUES
                                                    (86, 'DELUXE'), (87, 'DELUXE'), (88, 'DELUXE'), (89, 'DELUXE'), (90, 'DELUXE'),
                                                    (91, 'DELUXE'), (92, 'DELUXE'), (93, 'DELUXE'), (94, 'DELUXE'), (95, 'DELUXE');

-- 5 PENTHOUSE rooms (RoomID: 96 to 100)
INSERT OR REPLACE INTO Rooms (RoomID, RoomType) VALUES
                                                    (96, 'PENTHOUSE'), (97, 'PENTHOUSE'), (98, 'PENTHOUSE'), (99, 'PENTHOUSE'), (100, 'PENTHOUSE');


-- Create Reservations table
CREATE TABLE IF NOT EXISTS Reservations (
                                            ReservationID INTEGER PRIMARY KEY AUTOINCREMENT,
                                            GuestID INTEGER NOT NULL,
                                            CheckInDate DATE NOT NULL,
                                            CheckOutDate DATE NOT NULL,
                                            NumberOfGuests INTEGER NOT NULL,
                                            Status TEXT NOT NULL CHECK(Status IN ('CONFIRMED', 'CANCELLED', 'CHECKED_OUT')),
                                            FOREIGN KEY (GuestID) REFERENCES Guests(GuestID)
);

-- Create Reservation-Room mapping table
CREATE TABLE IF NOT EXISTS Reservations_Rooms (
                                                  ReservationID INTEGER NOT NULL,
                                                  RoomID INTEGER NOT NULL,
                                                  PRIMARY KEY (ReservationID, RoomID),
                                                  FOREIGN KEY (ReservationID) REFERENCES Reservations(ReservationID),
                                                  FOREIGN KEY (RoomID) REFERENCES Rooms(RoomID)
);

-- Create Billing table
CREATE TABLE IF NOT EXISTS Billing (
                                       BillID INTEGER PRIMARY KEY AUTOINCREMENT,
                                       ReservationID INTEGER NOT NULL,
                                       Amount REAL NOT NULL,
                                       Discount REAL DEFAULT 0.0 CHECK(Discount >= 0 AND Discount <= 25),
                                       Tax REAL NOT NULL,
                                       TotalAmount REAL NOT NULL,
                                       FOREIGN KEY (ReservationID) REFERENCES Reservations(ReservationID)
);

-- Create Feedback table
CREATE TABLE IF NOT EXISTS Feedback (
                                        GuestID INTEGER NOT NULL,
                                        ReservationID INTEGER NOT NULL,
                                        Comments TEXT NOT NULL,
                                        Rating REAL CHECK(Rating >= 0 AND Rating <= 5),
                                        PRIMARY KEY (GuestID, ReservationID),
                                        FOREIGN KEY (GuestID) REFERENCES Guests(GuestID),
                                        FOREIGN KEY (ReservationID) REFERENCES Reservations(ReservationID)
);
