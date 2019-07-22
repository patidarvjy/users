DELETE FROM public.team_memberships WHERE public.team_memberships.id IN (SELECT tm.id FROM public.team_memberships tm LEFT JOIN public.roles r on tm.role_id=r.id WHERE r.role_type = 'Custom');

DELETE FROM public.role_privileges where role_id in (SELECT role_id FROM public.role_privileges rp left join public.roles r on rp.role_id=r.id WHERE r.role_type = 'Custom');

DELETE FROM public.roles where role_type='Custom';