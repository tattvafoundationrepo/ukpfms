update egcl_servicedetails set function=(select functionid from egcl_service_accountdetails where egcl_service_accountdetails.servicedetails=egcl_servicedetails.id);
