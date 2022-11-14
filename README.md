# vpn-merge

## openvpn

    git submodule deinit -f vpn_core/ics-openvpn
    
    rm -rf .git/modules/vpn_core/ics-openvpn
    
    or windows
    
    rm -r -fo .git/modules/vpn_core/ics-openvpn
    
    git rm -f vpn_core/ics-openvpn
    
    git submodule add -b custom https://github.com/7449/ics-openvpn.git ./vpn_core/ics-openvpn

## strongswan

    git submodule deinit -f vpn_core/strongswan
    
    rm -rf .git/modules/vpn_core/strongswan
    
    or windows
    
    rm -rf -fo .git/modules/vpn_core/strongswan
    
    git rm -f vpn_core/strongswan
    
    git submodule add -b custom https://github.com/7449/strongswan.git ./vpn_core/strongswan