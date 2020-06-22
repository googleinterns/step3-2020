/**
 * Makes request to server
 * Sends index to an org
 * fetches names of three other orgs
 */
function findSimilarOrgs() {
  const orgIndex = document.getElementById('org-index').value;
  var indexQuery = updateQueryString('org_index', orgIndex);
  var queryString = '/predict?' + indexQuery;
  fetch(queryString).then(response => response.json()).then(text => {
    const relatedDiv = document.getElementById("related-section")
    relatedDiv.innerHTML='';
    console.log(text);
    text.forEach((orgName)=> {
      relatedDiv.appendChild(getOrgAsList(orgName));
    });
  })
}

/**
  * Creates list element from org name
 */
function getOrgAsHTML(orgName) {
  const orgElement = document.createElement('li');
  orgElement.innerText = orgName;
  return orgElement;
}

/**
 * Returns an updated URL search param
 */
function updateQueryString(key, value) {
  var searchParams = new URLSearchParams();
  searchParams.append(key, value);
  return searchParams;
}
