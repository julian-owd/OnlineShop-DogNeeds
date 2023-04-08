import { browser } from "$app/environment";

export const serverAddress = "localhost:8080";
export const defaultProductPicture = "/images/products/not_found.jpg";

/**
 * check if the session can be retrieved
 *  reply of server is true if it was possible
 */
export async function checkAuth() {
    let session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies;

    if (!session) {
        return false;
    }
    
    const request = await fetch("http://" + serverAddress + "/account/auth", {
        method: "POST",
        body: session
    }).catch(function () {
        return false;
    });
    const result = await request.text();
    return result === "true";
};