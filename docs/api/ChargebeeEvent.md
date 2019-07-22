# Chargebee Event Webhook
The chargebee event webhook serves as endpoint for callbacks from the 
chargebee event service.

Whenever an event triggers on the chargebee server, they make a call to
the specified webhook (`/chargebee-api/v2/event/`) with an [Chargebee Event](https://apidocs.chargebee.com/docs/api/events)
in their body. This object alone already provides all the information
for the handling of the event, but to prevent event forgery we make retrieve
the event from their servers again with the `id` specified in their request.

Right now we only handle 4 events: `customer_created`, `customer_deleted`,
`subscription_created`, and `subscription_cancelled`.

The customer related endpoints set/unset the `hasChargebeeAccount` flag
of the corresponding organisation.

`subscription_created` updates the subscription object with the corresponding
license. `subscription_cancelled` overwrites the subscription with a new
test license.
 
