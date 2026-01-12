CREATE TABLE item (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    stock_quantity INT NOT NULL
);

CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE "order" (
    id BIGSERIAL PRIMARY KEY,
    creation_date TIMESTAMP NOT NULL DEFAULT now(),
    item_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    fulfilled_quantity INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_order_item FOREIGN KEY (item_id) REFERENCES item(id),
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES "user"(id)
);

CREATE TABLE stock_movement (
    id BIGSERIAL PRIMARY KEY,
    creation_date TIMESTAMP NOT NULL DEFAULT now(),
    item_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_stock_item FOREIGN KEY (item_id) REFERENCES item(id)
);
