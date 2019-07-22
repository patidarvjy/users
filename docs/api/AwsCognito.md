Amazon Cognito supports developer authenticated identities, in addition to web identity federation through Facebook, Google, and Login with Amazon. With developer authenticated identities, you can register and authenticate users via your own existing authentication process, while still using Amazon Cognito to synchronize user data and access AWS resources. Using developer authenticated identities involves interaction between the end user device, your backend for authentication, and Amazon Cognito. 

`Define a Developer Provider Name and Associate it with an Identity Pool`

To use developer authenticated identities, you'll need an identity pool associated with your developer provider. To do so, follow these steps:

1.Log in to the Amazon Cognito console.
2.Create a new identity pool and, as part of the process, define a developer provider name in the Custom tab in Authentication Providers.
3.Alternatively, edit an existing identity pool and define a developer provider name in the Custom tab in Authentication Providers.

Note: Once the provider name has been set, it cannot be changed.
For additional instructions on working with the Amazon Cognito Console, see Using the Amazon Cognito Console.

`Implement an Identity Provider`
1. [Java Code Snippet](#1-java-code-snippet)
2. [Objective-c  Code Snippet](#2-objective-code-snippet)
3. [Swift Code Snippet](#3-swift-code-snippet)
4. [Getting a Token Server Side](#4-server-side-code) 
5. [End Point to Get Cognito Token](#5-end-point-to-get-cognito-token) 




## 1 Java Code Snippet

Below is a simple example of an identity provider which is used in our sample app:

```java 
public class DeveloperAuthenticationProvider extends AWSAbstractCognitoDeveloperIdentityProvider {

  private static final String developerProvider = "<Developer_provider_name>";

  public DeveloperAuthenticationProvider(String accountId, String identityPoolId, Regions region) {
    super(accountId, identityPoolId, region);
    // Initialize any other objects needed here.
  }

  // Return the developer provider name which you choose while setting up the
  // identity pool in the &COG; Console

  @Override
  public String getProviderName() {
    return developerProvider;
  }

  // Use the refresh method to communicate with your backend to get an
  // identityId and token.

  @Override
  public String refresh() {

    // Override the existing token
    setToken(null);

    // Get the identityId and token by making a call to your backend
    // (Call to your backend)

    // Call the update method with updated identityId and token to make sure
    // these are ready to be used from Credentials Provider.

    update(identityId, token);
    return token;

  }

  // If the app has a valid identityId return it, otherwise get a valid
  // identityId from your backend.

  @Override
  public String getIdentityId() {

    // Load the identityId from the cache
    identityId = cachedIdentityId;

    if (identityId == null) {
       // Call to your backend
    } else {
       return identityId;
    }

  }
}
```

To use this identity provider, you have to pass it into CognitoCachingCredentialsProvider. Here's an example:
```java
DeveloperAuthenticationProvider developerProvider = new DeveloperAuthenticationProvider( null, "IDENTITYPOOLID", context, Regions.USEAST1);
CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider( context, developerProvider, Regions.USEAST1);

```

Aws Sdk Maven Repositery  for JAVA: 
// https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk
compile group: 'com.amazonaws', name: 'aws-java-sdk', version: '1.11.150'

Aws Sdk Maven Repositery  for Android: 
// https://mvnrepository.com/artifact/com.amazonaws/aws-android-sdk-core
compile group: 'com.amazonaws', name: 'aws-android-sdk-core', version: '2.4.4'




## 2 Objective Code Snippet

To use developer authenticated identities, implement your own identity provider class which extends AWSCognitoCredentialsProviderHelper. Your identity provider class should return a response object containing the token as an attribute.

```objc 
@implementation DeveloperAuthenticatedIdentityProvider
/*
 * Use the token method to communicate with your backend to get an
 * identityId and token.
 */

- (AWSTask <NSString*>) token {
    //Write code to call your backend:
    //Pass username/password to backend or some sort of token to authenticate user
    //If successful, from backend call getOpenIdTokenForDeveloperIdentity with logins map 
    //containing "your.provider.name":"enduser.username"
    //Return the identity id and token to client
    //You can use AWSTaskCompletionSource to do this asynchronously

    // Set the identity id and return the token
    self.identityId = response.identityId;
    return [AWSTask taskWithResult:response.token];
}

@end

```

To use this identity provider, pass it into AWSCognitoCredentialsProvider as shown in the following example:

```objc 
DeveloperAuthenticatedIdentityProvider * devAuth = [[DeveloperAuthenticatedIdentityProvider alloc] initWithRegionType:AWSRegionYOUR_IDENTITY_POOL_REGION 
                                         identityPoolId:@"YOUR_IDENTITY_POOL_ID"
                                        useEnhancedFlow:YES
                                identityProviderManager:nil];
AWSCognitoCredentialsProvider *credentialsProvider = [[AWSCognitoCredentialsProvider alloc]
                                                          initWithRegionType:AWSRegionYOUR_IDENTITY_POOL_REGION
                                                          identityProvider:devAuth];
```
If you want to support both unauthenticated identities and developer authenticated identities, override the logins method in your AWSCognitoCredentialsProviderHelper implementation.


```objc 
- (AWSTask<NSDictionary<NSString *, NSString *> *> *)logins {
    if(/*logic to determine if user is unauthenticated*/) {
        return [AWSTask taskWithResult:nil];
    }else{
        return [super logins];
    }
}
```

If you want to support developer authenticated identities and social providers, you must manage who the current provider is in your logins implementation of AWSCognitoCredentialsProviderHelper.

```objc    
- (AWSTask<NSDictionary<NSString *, NSString *> *> *)logins {
    if(/*logic to determine if user is unauthenticated*/) {
        return [AWSTask taskWithResult:nil];
    }else if (/*logic to determine if user is Facebook*/){
        return [AWSTask taskWithResult: @{ AWSIdentityProviderFacebook : [FBSDKAccessToken currentAccessToken] }];
    }else {
        return [super logins];
    }
}
```
`GitHub Link for aws-sdk-ios`  https://github.com/aws/aws-sdk-ios



## 3 Swift Code Snippet

To use developer authenticated identities, implement your own identity provider class which extends AWSCognitoCredentialsProviderHelper. Your identity provider class should return a response object containing the token as an attribute.

```swift 
import AWSCore
/*
 * Use the token method to communicate with your backend to get an
 * identityId and token.
 */
class DeveloperAuthenticatedIdentityProvider : AWSCognitoCredentialsProviderHelper {
    override func token() -> AWSTask<NSString> {
    //Write code to call your backend:
    //pass username/password to backend or some sort of token to authenticate user, if successful, 
    //from backend call getOpenIdTokenForDeveloperIdentity with logins map containing "your.provider.name":"enduser.username"
    //return the identity id and token to client
    //You can use AWSTaskCompletionSource to do this asynchronously

    // Set the identity id and return the token
    self.identityId = resultFromAbove.identityId
    return AWSTask(result: resultFromAbove.token)
}
```

To use this identity provider, pass it into AWSCognitoCredentialsProvider as shown in the following example:
```swift 
let devAuth = DeveloperAuthenticatedIdentityProvider(regionType: .YOUR_IDENTITY_POOL_REGION, identityPoolId: "YOUR_IDENTITY_POOL_ID", useEnhancedFlow: true, identityProviderManager:nil)
let credentialsProvider = AWSCognitoCredentialsProvider(regionType: .YOUR_IDENTITY_POOL_REGION, identityProvider:devAuth)
let configuration = AWSServiceConfiguration(region: .YOUR_IDENTITY_POOL_REGION, credentialsProvider:credentialsProvider)
AWSServiceManager.default().defaultServiceConfiguration = configuration
```

If you want to support both unauthenticated identities and developer authenticated identities, override the logins method in your AWSCognitoCredentialsProviderHelper implementation.

```swift 
override func logins () -> AWSTask<NSDictionary> {
    if(/*logic to determine if user is unauthenticated*/) {
        return AWSTask(result:nil)
    }else {
        return super.logins()
    }
}
```

If you want to support developer authenticated identities and social providers, you must manage who the current provider is in your logins implementation of AWSCognitoCredentialsProviderHelper.

```swift 
override func logins () -> AWSTask<NSDictionary> {
    if(/*logic to determine if user is unauthenticated*/) {
        return AWSTask(result:nil)
    }else if (/*logic to determine if user is Facebook*/){
        if let token = AccessToken.current?.authenticationToken {
            return AWSTask(result: [AWSIdentityProviderFacebook:token])
        }
        return AWSTask(error:NSError(domain: "Facebook Login", code: -1 , userInfo: ["Facebook" : "No current Facebook access token"]))
    }else {
        return super.logins()
    }
}
```

`GitHub Link for aws-sdk-ios`  https://github.com/aws/aws-sdk-ios

## 4 Server Side Code

```java 
// authenticate your end user as appropriate
// ....

// if authenticated, initialize a cognito client with your AWS developer credentials
AmazonCognitoIdentity identityClient = new AmazonCognitoIdentityClient(
  new BasicAWSCredentials("access_key_id", "secret_access_key")
);

// create a new request to retrieve the token for your end user
GetOpenIdTokenForDeveloperIdentityRequest request =
  new GetOpenIdTokenForDeveloperIdentityRequest();
request.setIdentityPoolId("YOUR_COGNITO_IDENTITY_POOL_ID");

request.setIdentityId("YOUR_COGNITO_IDENTITY_ID"); //optional, set this if your client has an
                                                   //identity ID that you want to link to this 
                                                   //developer account

// set up your logins map with the username of your end user
HashMap<String,String> logins = new HashMap<>();
logins.add("YOUR_IDENTITY_PROVIDER_NAME","YOUR_END_USER_IDENTIFIER");
request.setLogins(logins);

// optionally set token duration (in seconds)
request.setTokenDuration(60 * 15l);
GetOpenIdTokenForDeveloperIdentityResult response =
  identityClient.getOpenIdTokenForDeveloperIdentity(request);

// obtain identity id and token to return to your client
String identityId = response.getIdentityId();
String token = response.getToken();

//code to return identity id and token to client
//...

 ``` 


## 5 End Point To Get Cognito Token 
`POST /api/v1/me/storage/token`

Retrieve the Cognito Token information.

#### Response

```json
{
    "token": "token to access aws servives",
    "identityId": "identityId"
}
```

| Status Code | Description |
| ---------------- | ---------------- |
| `200` OK | The response from above is in the body, everything went fine. |
| `401` Unauthorized | The request has to be authenticated! |

