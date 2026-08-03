CREATE TABLE orbit_mean_elements (
    norad_cat_id BIGINT NOT NULL,
    object_name VARCHAR(128) NOT NULL,
    object_id VARCHAR(32) NOT NULL,
    epoch TIMESTAMP NOT NULL,
    mean_motion DOUBLE PRECISION NOT NULL,
    eccentricity DOUBLE PRECISION NOT NULL,
    inclination DOUBLE PRECISION NOT NULL,
    ra_of_asc_node DOUBLE PRECISION NOT NULL,
    arg_of_pericenter DOUBLE PRECISION NOT NULL,
    mean_anomaly DOUBLE PRECISION NOT NULL,
    ephemeris_type BIGINT NOT NULL,
    classification_type CHAR(1) NOT NULL,
    element_set_no BIGINT NOT NULL,
    rev_at_epoch BIGINT NOT NULL,
    bstar DOUBLE PRECISION NOT NULL,
    mean_motion_dot DOUBLE PRECISION NOT NULL,
    mean_motion_ddot DOUBLE PRECISION NOT NULL,

    CONSTRAINT "PK_OrbitMean_elements_primary_key" PRIMARY KEY (norad_cat_id)
);

CREATE TABLE satellite_groups (
    norad_cat_id BIGINT NOT NULL,
    group_name VARCHAR(32) NOT NULL,

    CONSTRAINT "PK_satellite_groups" PRIMARY KEY (norad_cat_id, group_name),
    CONSTRAINT fk_satellite_groups_norad_cat_id
        FOREIGN KEY (norad_cat_id) REFERENCES orbit_mean_elements (norad_cat_id)
);
