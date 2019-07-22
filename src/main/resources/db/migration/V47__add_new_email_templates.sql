UPDATE email_templates SET notification_type = 'EndTestPhase10Days' WHERE notification_type = 'EndTestPhase';
INSERT INTO email_templates VALUES
('e1ce6138-f0b0-429b-8aa9-fcb8708aeac3'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, 'EndTestPhase3Days', true, 'en'),
('04855b99-86a7-4ff3-a951-df00ad96fbb7'::uuid, '00000000-0000-0000-0000-000000000000'::uuid, 'EndTestPhase3Days', true, 'de');
