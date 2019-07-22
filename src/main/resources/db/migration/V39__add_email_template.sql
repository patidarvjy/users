CREATE TABLE email_templates (
    send_grid_template_id UUID PRIMARY KEY NOT NULL,
    organisation_id UUID NOT NULL,
    notification_type TEXT NOT NULL,
    default_template bool NOT NULL,
    language TEXT NOT NULL
);