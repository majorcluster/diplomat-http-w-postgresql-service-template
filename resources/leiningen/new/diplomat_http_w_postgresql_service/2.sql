CREATE TABLE IF NOT EXISTS event_polls (
                                     id bigserial PRIMARY KEY,
                                     title varchar(100) NOT NULL,
                                     active boolean NOT NULL DEFAULT TRUE,
                                     voted text array,
                                     created_at timestamp NOT NULL,
                                     event_id bigserial NOT NULL,

                                     FOREIGN KEY(event_id) REFERENCES events(id)
);
