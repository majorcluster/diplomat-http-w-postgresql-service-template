CREATE TABLE IF NOT EXISTS events (
                                     id bigserial PRIMARY KEY,
                                     title varchar(100) NOT NULL,
                                     city varchar(40) NOT NULL,
                                     location varchar(100) NOt NULL,
                                     scheduled_at timestamp NOT NULL,
                                     confirmed text array,
                                     absent text array
);
