package barddataexport.util

import javax.servlet.http.HttpServletRequest
import org.codehaus.groovy.grails.validation.routines.InetAddressValidator
import org.codehaus.groovy.grails.plugins.codecs.MD5BytesCodec
import java.security.MessageDigest

class AuthenticationService {

    def grailsApplication

    /**
     * Used by the BardDataExportFilters filter to check authentication of the incoming request
     * against an IP address whitelist and an APIKEY hash code.
     *
     * Get the APIKEY from the incoming request's header (custom key).
     *
     * @param request
     * @return
     */
    Boolean authenticate(HttpServletRequest request) {
//        InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance()

        String apiKey = grailsApplication.config.barddataexport.externalapplication.apiKey.hashed
        String requestApiKey = request.getHeader('APIKEY')
        if (apiKey != requestApiKey)
            return false

        List<String> ipAddressWhiteList = grailsApplication.config.barddataexport.externalapplication.ipAddress.whiteList as List<String>
        String remoteIpAddress = request.getRemoteAddr()
//        assert inetAddressValidator.isValid(remoteIpAddress)

        for (String ipAddress in ipAddressWhiteList) {
//            assert inetAddressValidator.isValid(ipAddress)
//            assert ipAddress.split(/\./).size() == 4

            Boolean match = doIpAddressesMatch(remoteIpAddress, ipAddress)
            if (match) {
                final String requestParamsString = "URL: '${request.getRequestURL()}'; Remote address: '${request.getRemoteAddr()}'"
                log.info(requestParamsString)
                return true
            }
        }

        return false
    }

    /**
     * Compares two IP addresses to check if they match. Accepts wildcard ('*') as part(s) of an IP address.
     * For example, '1.*.3.4' would compare true both with '1.2.3.4' and '1.5.3.4'
     * There is also a support for IPv6 with wildcards (e.g., '1:2:3:4:5:6:7:*')
     *
     * @param rhs
     * @param lhs
     * @return
     */
    Boolean doIpAddressesMatch(String rhs, String lhs) {
        String[] rhsStringArray = rhs.split(/[\.:]/) //IPv6 uses ':' instead of '.'
        String[] lhsStringArray = lhs.split(/[\.:]/) //IPv6 uses ':' instead of '.'
        if (rhsStringArray.size() != lhsStringArray.size())
            return false

        for (int i in 0..<rhsStringArray.size()) { //IPv6 allows larger UP address fields. For example, when running locally we receive 0:0:0:0:0:0:0:1 as the remote address.
            if (rhsStringArray[i] == '*' || lhsStringArray[i] == '*')
                continue

            Integer lhsIntVal = new Integer(lhsStringArray[i])
            Integer rhsIntVal = new Integer(rhsStringArray[i])
            if (rhsIntVal != lhsIntVal)
                return false
        }

        return true
    }
}
