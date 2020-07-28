/**
 * Makes request to server
 * Sends index to an org
 * fetches names of three other orgs
 */
function findSimilarOrgs() {
  const orgIndex = document.getElementById('org-index').value;
  var indexQuery = updateQueryString('org_index', orgIndex);
  var queryString = '/predict?' + indexQuery;
  
  // remove previously displayed similar organizations
  var similarOrgs = document.getElementById('related-section');
  while (similarOrgs.firstChild) {
    similarOrgs.removeChild(similarOrgs.firstChild);
  }
  
  const relatedDiv = document.getElementById("related-section");
  fetch(queryString).then(response => response.json()).then(text => {  
    text.forEach((org) => {
      relatedDiv.appendChild(getOrgAsHtmlRelated(org, orgIndex));
    });
  })
}

function getOrgAsHtmlRelated(org, index) {
  if (Number(org.index) === Number(index)) {
    const liElement = document.createElement('li');
    const nameElement = document.createElement('h3');
    nameElement.innerText = 'Organizations similar to ' + org.name + ': ';
    liElement.appendChild(nameElement);
    return liElement;
  }
  const orgElement = document.createElement('li');
  const nameElement = document.createElement('h4');
  nameElement.innerText = org.index + '. ' + org.name;
  orgElement.appendChild(nameElement);
  return orgElement;
}

/**
 * Creates list element from org
 */
function getOrgAsHtmlDescription(org) {
  const orgElement = document.createElement('li');
  const nameElement = document.createElement('h4');
  nameElement.innerText = org.index + '. ' + org.name;
  orgElement.appendChild(nameElement);
  const textElement = document.createElement('p');
  textElement.innerText = org.statement;
  orgElement.appendChild(textElement);
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

function fetchOrgs() {
  fetch("/fetch-orgs").then(response => response.json()).then(text => {
    const orgsDiv = document.getElementById("existing-organizations");
    text.forEach(org => {
      orgsDiv.appendChild(getOrgAsHtmlDescription(org));
    });
  })
}
